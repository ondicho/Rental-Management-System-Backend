package ondicho.co.ke.RentalManangementSystemBackend.services.Property;

import jakarta.servlet.http.HttpServletRequest;
import ondicho.co.ke.RentalManangementSystemBackend.models.Auth.AuthenticationResponse;
import ondicho.co.ke.RentalManangementSystemBackend.models.Auth.User;
import ondicho.co.ke.RentalManangementSystemBackend.models.Property.*;
import ondicho.co.ke.RentalManangementSystemBackend.repositories.Property.*;
import ondicho.co.ke.RentalManangementSystemBackend.services.Auth.UserService;
import ondicho.co.ke.RentalManangementSystemBackend.util.ResponseHandler;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.time.LocalDate;
import java.util.*;

@Service
public class PropertyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyService.class);

    @Autowired
    ResponseHandler responseHandler;

    @Autowired
    PropertyRepository propertyRepository;
    @Autowired
    BlockRepository blockRepository;

    @Autowired
    FloorRepository floorRepository;

    @Autowired
    ApartmentRepository apartmentRepository;

    @Autowired
    ApartmentBillRepository apartmentBillRepository;

    @Autowired
    LandlordRepository landlordRepository;

    @Autowired
    TenantRepository tenantRepository;

    @Autowired
    PaymentAccountRepository paymentAccountRepository;

    @Autowired
    UserService userService;

    //    Create property to-do:
//    1.Check if landlord exists and is active
//    2.Create property
//    3.Register property block=>floors=>apartments=>tenants=>apartmentBills
//    4.Register payment accounts and register urls
//    5.
    public Map<String, Object> processExcel(MultipartFile file, HttpServletRequest request) {
        try {

            AuthenticationResponse token = (AuthenticationResponse) request.getAttribute("token");
            LOGGER.info("Beginning Excel data processing");

            XSSFWorkbook wb = new XSSFWorkbook(file.getInputStream());

            for (int sheetIndex = 0; sheetIndex < wb.getNumberOfSheets(); sheetIndex++) {
                XSSFSheet sheet = wb.getSheetAt(sheetIndex);
                String sheetName = sheet.getSheetName();
                LOGGER.info("Beginning Excel sheet: " + sheetName + " processing");
                processSheet(sheet, token);
                sheetIndex++;
            }


            LOGGER.info("Create Property: Complete");

            return responseHandler.generateResponse("sucess", null, null);

        } catch (Exception e) {
            LOGGER.error("Error processing excel: {}", e.getMessage(), e);
            return responseHandler.generateResponse("fail", null, e.getMessage());
        }
    }

    private String getStringCellValue(Cell cell) {
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((int) cell.getNumericCellValue());
        } else {
            return ""; // Handle other cell types as needed
        }
    }

    private Map<String, Object> processSheet(XSSFSheet sheet, AuthenticationResponse token) {
        // Process the header row
//         String sheetName = sheet.getSheetName();

        String sheetName = sheet.getSheetName();
        Row headerRow = sheet.getRow(0);
        List<String> headers = new ArrayList<>();
        for (Cell cell : headerRow) {
            headers.add(cell.getStringCellValue());
        }

        if (sheetName.equalsIgnoreCase("properties")) {
            Iterator<Row> rowIterator = sheet.iterator();
            rowIterator.next(); // Skip the header row
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Map<String, Object> rowData = extractRowData(row, headers);
                processPropertyData(rowData, token);
            }
        } else if (sheetName.equalsIgnoreCase("tenants")) {
            Iterator<Row> rowIterator = sheet.iterator();
            rowIterator.next(); // Skip the header row
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Map<String, Object> rowData = extractRowData(row, headers);
                processBlockData(rowData, token);
            }
        } else if (sheetName.equalsIgnoreCase("water")) {
            Iterator<Row> rowIterator = sheet.iterator();
            rowIterator.next(); // Skip the header row
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Map<String, Object> rowData = extractRowData(row, headers);
                processWaterBill(rowData, token);
            }
        } else {
            return responseHandler.generateResponse("fail", null, "Unknown Sheet");
        }
        return null;
    }

    private Map<String, Object> extractRowData(Row row, List<String> headers) {
        Map<String, Object> rowData = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null) {
                String columnHeader = headers.get(i);
                if (columnHeader != null) {
                    Object cellValue;
                    switch (cell.getCellType()) {
                        case STRING:
                            cellValue = cell.getStringCellValue().trim();
                            break;
                        case NUMERIC:
                            if (DateUtil.isCellDateFormatted(cell)) {
                                cellValue = cell.getDateCellValue();
                            } else {
                                cellValue = (int) cell.getNumericCellValue();
                            }
                            break;
                        case BOOLEAN:
                            cellValue = cell.getBooleanCellValue();
                            break;
                        case FORMULA:
                            cellValue = cell.getCellFormula();
                            break;
                        default:
                            cellValue = null;
                    }
                    rowData.put(columnHeader, cellValue);
                }
            }
        }
        return rowData;
    }

    private User getUser(AuthenticationResponse token) {
        return userService.fetchUser(token.getEmail());
    }


    private void processPropertyData(Map<String, Object> rowData, AuthenticationResponse token) {
        User user = getUser(token);
        if (user != null) {
            Landlord landlord = getOrCreateLandlord(user);
            LOGGER.info("Create Landlord: success");

            Property property = getOrCreateProperty(rowData, landlord);
            if (property != null) {
                addOrUpdatePaymentAccount(rowData, property);
                propertyRepository.save(property);
                LOGGER.info("Existing property found and payment account added");
            } else {
                LOGGER.info("Create new Property: success");
            }

            updateLandlordProperties(landlord);
            LOGGER.info("All landlord properties updated : success");
        }
    }

    private void processBlockData(Map<String, Object> rowData, AuthenticationResponse token) {
        User user = getUser(token);
        if (user != null) {
            Landlord landlord = getOrCreateLandlord(user);
            Property property = getOrCreateProperty(rowData, landlord);

            if (property != null) {
                Block block = getOrCreateBlock(property, rowData.get("block").toString());
                if (block != null) {
                    Floor floor = getOrCreateFloor(block, rowData.get("floor").toString());
                    if (floor != null) {
                        Apartment apartment = getOrCreateApartment(floor, rowData.get("apartment").toString());
                        if (apartment != null) {
                            createOrUpdateTenant(
                                    apartment, rowData.get("tenant_name").toString(), rowData.get("id_number").toString(),
                                    rowData.get("phone_number").toString(), Collections.singletonList(rowData.get("other_phone_numbers").toString()),
                                    (LocalDate) rowData.get("move_in_date"));
                        }
                    }
                }
            }
        }
    }

    private void processWaterBill(Map<String, Object> rowData, AuthenticationResponse token) {
    }

    private Landlord getOrCreateLandlord(User user) {
        Optional<Landlord> existingLandlord = landlordRepository.findByUserAccount(user.getId());
        if (existingLandlord.isEmpty()) {
            Landlord landlord = Landlord.builder()
                    .userAccount(user)
                    .build();
            return landlordRepository.save(landlord);
        }
        return existingLandlord.get();
    }

    private Property getOrCreateProperty(Map<String, Object> rowData, Landlord landlord) {
        Optional<Property> existingPropertyOptional = propertyRepository.findByName(rowData.get("property_name").toString().toUpperCase());
        if (existingPropertyOptional.isPresent()) {
            return existingPropertyOptional.get();
        }

        Property property = Property.builder()
                .name(rowData.get("property_name").toString())
                .landlord(landlord)
                .build();
        return propertyRepository.save(property);
    }

    private void addOrUpdatePaymentAccount(Map<String, Object> rowData, Property property) {
        String accountTypeString = rowData.get("account_type").toString();
        String shortCode = rowData.get("shortcode").toString();
        String accountNumber = rowData.get("account_number") != null ? rowData.get("account_number").toString() : null;
        PaymentAccountType accountType = PaymentAccountType.getByName(accountTypeString);
        ApartmentBillType billType = ApartmentBillType.valueOf(rowData.get("purpose").toString());

        PaymentAccount.PaymentAccountBuilder builder = PaymentAccount.builder()
                .shortCode(shortCode)
                .accountType(accountType)
                .billType(billType)
                .property(property);

        // Conditionally set accountNumber only if it is not null
        if (accountNumber != null) {
            builder.accountNumber(accountNumber);
        }

        PaymentAccount paymentAccount = builder.build();

        // Adjusted call to checkPayPaymentAccountExists to handle null accountNumber
        if (checkPaymentAccountExists(shortCode, accountNumber)) {
            paymentAccount = paymentAccountRepository.save(paymentAccount);
            property.getPaymentAccounts().add(paymentAccount);
        }
    }


    public boolean checkPaymentAccountExists(String shortCode, String accountNumber) {
        boolean existsByShortCodeAndAccountNumber = false;
        boolean existsByShortCodeAndAsTill = false;

        // Check if accountNumber is not null before calling the repository method
        if (accountNumber != null) {
            existsByShortCodeAndAccountNumber = paymentAccountRepository.existsByShortCodeAndAccountNumber(shortCode, accountNumber);
        }

        int accountTypeOrdinal = PaymentAccountType.getByName("Buy Goods").ordinal();
        existsByShortCodeAndAsTill = paymentAccountRepository.existsByShortCodeAndAsTill(shortCode, accountTypeOrdinal);


        // Return true if either condition is met
        return existsByShortCodeAndAccountNumber || existsByShortCodeAndAsTill;
    }


    private void updateLandlordProperties(Landlord landlord) {
        landlord.setProperties(landlord.getProperties());
        landlordRepository.save(landlord);
    }


    private Block getOrCreateBlock(Property property, String blockName) {
        Optional<Block> existingBlock = property.getBlocks().stream()
                .filter(block -> block.getName().equals(blockName))
                .findAny();
        if (existingBlock.isPresent()) {
            return existingBlock.get();
        } else {
            Block block = new Block();
            block.setName(blockName);
            block.setProperty(property);
            return blockRepository.save(block);
        }
    }

    private Floor getOrCreateFloor(Block block, String floorName) {
        Optional<Floor> existingFloor = block.getFloors().stream()
                .filter(floor -> floor.getName().equals(floorName))
                .findAny();
        if (existingFloor.isPresent()) {
            return existingFloor.get();
        } else {
            Floor floor = new Floor();
            floor.setName(floorName);
            floor.setBlock(block);
            return floorRepository.save(floor);
        }
    }

    private Apartment getOrCreateApartment(Floor floor, String apartmentName) {
        Optional<Apartment> existingApartment = floor.getApartments().stream()
                .filter(apartment -> apartment.getName().equals(apartmentName))
                .findAny();
        if (existingApartment.isPresent()) {
            return existingApartment.get();
        } else {
            Apartment apartment = new Apartment();
            apartment.setName(apartmentName);
            apartment.setFloor(floor);
            return apartmentRepository.save(apartment);
        }
    }

    private void createOrUpdateTenant(Apartment apartment, String tenantName, String idNumber, String phoneNumber, List<String> otherPhoneNumbers, LocalDate moveInDate) {
        Tenant tenant = new Tenant();
        tenant.setName(tenantName);
        tenant.setIdNumber(idNumber);
        tenant.setPhoneNumber(phoneNumber);
        tenant.setOtherPhoneNumbers(otherPhoneNumbers);
        tenant.setMoveInDate(moveInDate);
        tenant.setApartment(apartment);
        tenantRepository.save(tenant);
    }


    public Map<String, Object> fetchProperties(int pageNo, int pageSize, String sortBy) {
        try {

            Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));
            Page<Property> properties = propertyRepository.findAll(pageable);

            return responseHandler.generateResponse("success", properties, null);
        } catch (Exception e) {
            return responseHandler.generateResponse("fail", null, e.getMessage());
        }
    }


}
