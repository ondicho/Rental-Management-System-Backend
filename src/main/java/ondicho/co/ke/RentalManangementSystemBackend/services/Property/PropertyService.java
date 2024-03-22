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
                processSheet(sheet, token);
                sheetIndex++;
            }


            LOGGER.info("Create Property: Complete");

            return responseHandler.generateResponse("sucess", null, null);

        } catch (Exception e) {
            LOGGER.error("Error processing excel: {}", e.getMessage());
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
        LOGGER.info("Beginning Excel sheet: " + sheetName + " processing");
        Row headerRow = sheet.getRow(0);
        List<String> headers = new ArrayList<>();
        for (Cell cell : headerRow) {
            headers.add(cell.getStringCellValue());
        }

        // Process the rest of the rows
        Iterator<Row> rowIterator = sheet.iterator();
        rowIterator.next(); // Skip the header row
        while (rowIterator.hasNext()) {
            LOGGER.info("Excel sheet: " + sheetName + " rowIterator.hasNext()");
            Row row = rowIterator.next();
            Map<String, Object> rowData = extractRowData(row, headers);
            LOGGER.info("Excel sheet: " + sheetName + " record size : "+rowData.size());
            if (sheetName.equalsIgnoreCase("properties")) {
                processPropertyData(rowData, token);
            } else if (sheetName.equalsIgnoreCase("tenants")) {
                processBlockData(rowData, token);
            } else if (sheetName.equalsIgnoreCase("water")) {
                processWaterBill(rowData, token);
            } else {
                return responseHandler.generateResponse("fail", null, "Unkown Sheet");
            }
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


    private void processPropertyData(Map<String, Object> rowData, AuthenticationResponse token) {
        Set<Property> properties = new HashSet<Property>();
        User user = userService.fetchUser(token.getEmail());
        if (user != null) {
            Optional<Landlord> existingLandlord=landlordRepository.findByUserAccount(user.getId());
            if(existingLandlord.isEmpty()) {
//                    check if landlord exists
                Landlord landlord = Landlord.builder()
                        .userAccount(user)
                        .build();

                landlord = landlordRepository.save(landlord);
            }
            Landlord landlord=existingLandlord.get();

            LOGGER.info("Create Landlord: success");
            // Map rowData to your models based on the sheet name
//                    if ("properties".equalsIgnoreCase(sheetName)) {

//                    check if property with same name exists,if it does add details of row as another payment type
            Optional<Property> existingPropertyOptional = propertyRepository.findByName(rowData.get("property_name").toString().toUpperCase());

            String accountTypeString =rowData.get("account_type").toString();
            PaymentAccountType accountType = PaymentAccountType.getByName(accountTypeString);

            if (existingPropertyOptional.isPresent()) {
                Property existingProperty = existingPropertyOptional.get();
                Set<PaymentAccount> paymentAccounts = existingProperty.getPaymentAccounts();
                PaymentAccount paymentAccount = PaymentAccount.builder()
                        .shortCode(rowData.get("shortcode").toString())
                        .accountNumber(rowData.get("account_number").toString())
                        .accountType(accountType)
                        .billType(ApartmentBillType.valueOf(rowData.get("purpose").toString()))
                        .property(existingProperty)
                        .build();
                paymentAccount = paymentAccountRepository.save(paymentAccount);
                paymentAccounts.add(paymentAccount);

                existingProperty.setPaymentAccounts(paymentAccounts);

                propertyRepository.save(existingProperty);
                LOGGER.info("Existing property found and payment account added");
                properties.add(existingProperty);

            } else {
                Property property = Property.builder()
                        .name(rowData.get("property_name").toString())
                        .landlord(landlord)
                        .build();

                property = propertyRepository.save(property);
                LOGGER.info("Create new Property: success");

                Set<PaymentAccount> paymentAccounts = property.getPaymentAccounts();
                PaymentAccount paymentAccount = PaymentAccount.builder()
                        .shortCode(rowData.get("shortcode").toString())
                        .accountNumber(rowData.get("account_number").toString())
                        .accountType(accountType)
                        .billType(ApartmentBillType.valueOf(rowData.get("purpose").toString()))
                        .property(property)
                        .build();
                paymentAccount = paymentAccountRepository.save(paymentAccount);
                paymentAccounts.add(paymentAccount);

                property.setPaymentAccounts(paymentAccounts);

                property = propertyRepository.save(property);

                properties.add(property);
            }
            landlord.setProperties(properties);
            landlordRepository.save(landlord);
            LOGGER.info("All landlord properties updated : success");
        }
    }


    private void processBlockData(Map<String, Object> rowData, AuthenticationResponse token) {

        String propertyName = rowData.get("property_name").toString();
        String blockName = rowData.get("block").toString();
        String floorName = rowData.get("floor").toString();
        String apartmentName = rowData.get("apartment").toString();
        String tenantName = rowData.get("tenant_name").toString();
        String idNumber = rowData.get("id_number").toString();
        String phoneNumber = rowData.get("phone_number").toString();
        String otherPhoneNumbers = rowData.get("other_phone_numbers").toString();
        String moveInDate = rowData.get("move_in_date").toString();
        long rent = (long) rowData.get("rent");
        long garbage = (long) rowData.get("garbage");
        long serviceCharge = (long) rowData.get("service_charge");

        Optional<Property> propertyOptional = propertyRepository.findByName(propertyName.toUpperCase());
        if (propertyOptional.isPresent()) {
            LOGGER.info("Property found");
            Property property = propertyOptional.get();
            Set<Block> blocks = property.getBlocks();
//                    use stream to search for block name
            Optional<Block> existingBlock = blocks.stream()
                    .filter(block -> block.getName().equals(blockName))
                    .findAny();
            if (existingBlock.isPresent()) {
                LOGGER.info("Block found");
                Block block = existingBlock.get();
                Set<Floor> floors = block.getFloors();
//                        use stream to search for floor name

                Optional<Floor> existingFloor = floors.stream()
                        .filter(floor -> floor.getName().equals(floorName))
                        .findAny();
                if (existingFloor.isPresent()) {
                    LOGGER.info("Floor found");
                    Floor floor = existingFloor.get();

                    Set<Apartment> apartments = floor.getApartments();

                    Optional<Apartment> existingApartment = apartments.stream()
                            .filter(apartment -> apartment.getName().equals(apartmentName))
                            .findAny();
                    if (existingApartment.isPresent()) {
//                                check apartment bills
                        LOGGER.info("Apartment found");

                    } else {
                        LOGGER.info("Apartment not found");
                        Apartment apartment = Apartment.builder()
                                .name(apartmentName)
                                .floor(floor)
                                .build();
                        apartment = apartmentRepository.save(apartment);
                        Set<ApartmentBill> apartmentBills = apartment.getApartmentBills();

                            ApartmentBill rentBill = ApartmentBill.builder()
                                    .billType(ApartmentBillType.rent)
                                    .billAmount(rent)
                                    .apartment(apartment)
                                    .fixed(true)
                                    .build();
                            apartmentBills.add(rentBill);

                            ApartmentBill garbageBill = ApartmentBill.builder()
                                    .billType(ApartmentBillType.garbage)
                                    .billAmount(garbage)
                                    .apartment(apartment)
                                    .fixed(true)
                                    .build();
                            apartmentBills.add(garbageBill);
                            ApartmentBill serviceChargeBill = ApartmentBill.builder()
                                    .billType(ApartmentBillType.service_charge)
                                    .billAmount(serviceCharge)
                                    .apartment(apartment)
                                    .fixed(true)
                                    .build();
                            apartmentBills.add(serviceChargeBill);
                        LOGGER.info("Bills created");
//                                create Tenant
                        Tenant tenant = Tenant.builder()
                                .name(tenantName)
                                .phoneNumber(phoneNumber)
                                .apartment(apartment)
                                .build();

                        tenant = tenantRepository.save(tenant);

                        apartment.setApartmentBills(apartmentBills);
                        apartment.setTenant(tenant);
                        apartment.setOccupancy(ApartmentOccupancy.occupied);

                        apartmentRepository.save(apartment);
                        apartments.add(apartment);

                        LOGGER.info("Tenant created");
                    }
                    floor.setApartments(apartments);
                    floorRepository.save(floor);

                    LOGGER.info("Floor updated");
                } else {
                    LOGGER.info("Floor not found");
                    Floor floor = Floor.builder()
                            .name(floorName)
                            .block(block)
                            .build();
                    floor = floorRepository.save(floor);

                    Set<Apartment> apartments = floor.getApartments();

                    Optional<Apartment> existingApartment = apartments.stream()
                            .filter(apartment -> apartment.getName().equals(apartmentName))
                            .findAny();
                    if (existingApartment.isPresent()) {
                        LOGGER.info("Apartment found");
                    } else {
                        LOGGER.info("Apartment not found");
                        Apartment apartment = Apartment.builder()
                                .name(apartmentName)
                                .floor(floor)
                                .build();
                        apartment = apartmentRepository.save(apartment);
                        Set<ApartmentBill> apartmentBills = apartment.getApartmentBills();
                            ApartmentBill rentBill = ApartmentBill.builder()
                                    .billType(ApartmentBillType.rent)
                                    .billAmount(rent)
                                    .apartment(apartment)
                                    .fixed(true)
                                    .build();
                            apartmentBills.add(rentBill);

                            ApartmentBill garbageBill = ApartmentBill.builder()
                                    .billType(ApartmentBillType.garbage)
                                    .billAmount(garbage)
                                    .apartment(apartment)
                                    .fixed(true)
                                    .build();
                            apartmentBills.add(garbageBill);
                            ApartmentBill serviceChargeBill = ApartmentBill.builder()
                                    .billType(ApartmentBillType.service_charge)
                                    .billAmount(serviceCharge)
                                    .apartment(apartment)
                                    .fixed(true)
                                    .build();
                            apartmentBills.add(serviceChargeBill);

                        LOGGER.info("Bills added");
//                                create Tenant
                        Tenant tenant = Tenant.builder()
                                .name(tenantName)
                                .phoneNumber(phoneNumber)
                                .apartment(apartment)
                                .build();

                        tenant = tenantRepository.save(tenant);

                        apartment.setApartmentBills(apartmentBills);
                        apartment.setTenant(tenant);
                        apartment.setOccupancy(ApartmentOccupancy.occupied);

                        apartmentRepository.save(apartment);
                        apartments.add(apartment);
                    }
                    floor.setApartments(apartments);
                    floorRepository.save(floor);
                    LOGGER.info("Tenant added");
                }
            } else {
                LOGGER.info("Block not found");
                // If the block does not exist, create a new one
                Block block = Block.builder()
                        .name(blockName)
                        .property(property)
                        .build();
                block = blockRepository.save(block);
                blocks.add(block);
                property.setBlocks(blocks);
                propertyRepository.save(property);

                LOGGER.info("Property block information updated");
                Set<Floor> floors = block.getFloors();
                // Create a new Floor within the newly created Block
                Floor floor = Floor.builder()
                        .name(floorName)
                        .block(block)
                        .build();
                floor = floorRepository.save(floor);
                floors.add(floor);
                block.setFloors(floors);
                blockRepository.save(block);
                LOGGER.info("Block floor information updated");

                Set<Apartment> apartments = floor.getApartments();

                // Create a new Apartment within the newly created Floor
                Apartment apartment = Apartment.builder()
                        .name(apartmentName)
                        .floor(floor)
                        .build();
                apartment = apartmentRepository.save(apartment);

                // Add ApartmentBills for rent, garbage, and service charge if they are not null
                Set<ApartmentBill> apartmentBills = new HashSet<>();
                    ApartmentBill rentBill = ApartmentBill.builder()
                            .billType(ApartmentBillType.rent)
                            .billAmount(rent)
                            .apartment(apartment)
                            .fixed(true)
                            .build();
                    apartmentBills.add(rentBill);

                    ApartmentBill garbageBill = ApartmentBill.builder()
                            .billType(ApartmentBillType.garbage)
                            .billAmount(garbage)
                            .apartment(apartment)
                            .fixed(true)
                            .build();
                    apartmentBills.add(garbageBill);

                    ApartmentBill serviceChargeBill = ApartmentBill.builder()
                            .billType(ApartmentBillType.service_charge)
                            .billAmount(serviceCharge)
                            .apartment(apartment)
                            .fixed(true)
                            .build();
                    apartmentBills.add(serviceChargeBill);

                apartment.setApartmentBills(apartmentBills);

                // Create a Tenant and associate it with the Apartment
                Tenant tenant = Tenant.builder()
                        .name(tenantName)
                        .phoneNumber(phoneNumber)
                        .apartment(apartment)
                        .build();
                tenant = tenantRepository.save(tenant);
                apartment.setTenant(tenant);
                apartment.setOccupancy(ApartmentOccupancy.occupied);

                // Save the updated Apartment
                apartmentRepository.save(apartment);
                apartments.add(apartment);
                floor.setApartments(apartments);
                floorRepository.save(floor);

                LOGGER.info("Floor apartment information updated");
            }
        }

    }


    private void processWaterBill(Map<String, Object> rowData, AuthenticationResponse token) {
    }

    public Map<String,Object> fetchProperties(int pageNo, int pageSize, String sortBy) {
        try {

            Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));
            Page<Property> properties = propertyRepository.findAll(pageable);

            return responseHandler.generateResponse("success", properties, null);
        } catch (Exception e) {
            return responseHandler.generateResponse("fail", null, e.getMessage());
        }
    }


}
