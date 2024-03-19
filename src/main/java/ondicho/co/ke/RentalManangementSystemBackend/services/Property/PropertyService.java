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
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<Map<String, Object>> createProperty(MultipartFile file, HttpServletRequest request) {
        try {

            AuthenticationResponse token = (AuthenticationResponse) request.getAttribute("token");
            LOGGER.info("Create Property: Start");
            LOGGER.info("Beginning Excel data processing");

            XSSFWorkbook wb = new XSSFWorkbook(file.getInputStream());
            Set<Property> properties = new HashSet<Property>();
            List<Tenant> tenants = new ArrayList<>();

//            for (int sheetIndex = 0; sheetIndex < wb.getNumberOfSheets(); sheetIndex++) {
            XSSFSheet ws = wb.getSheetAt(0);
            String sheetName = ws.getSheetName();

            // Process the header row
            Row headerRow = ws.getRow(0);
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(cell.getStringCellValue());
            }

            // Process the rest of the rows
            Iterator<Row> rowIterator = ws.iterator();
            rowIterator.next(); // Skip the header row
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Map<String, String> rowData = new HashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    Cell cell = row.getCell(i);
                    if (cell != null) {
                        String columnHeader = headers.get(i);
                        if (columnHeader != null) {
                            String cellValue = cell.getStringCellValue().trim(); // Trim whitespaces
                            rowData.put(columnHeader, cellValue);
                        }
                    }
                }
//                    register property
                User user = userService.fetchUser(token.getEmail());
                if (user != null) {
//                    check if landlord exists
                    Landlord landlord = Landlord.builder()
                            .userAccount(user)
                            .build();

                    landlord = landlordRepository.save(landlord);

                    LOGGER.info("Create Landlord: success");
                    // Map rowData to your models based on the sheet name
//                    if ("properties".equalsIgnoreCase(sheetName)) {

//                    check if property with same name exists,if it does add details of row as another payment type
                    Optional<Property> existingPropertyOptional = propertyRepository.findByName(rowData.get("property_name").toUpperCase());

                    if (existingPropertyOptional.isPresent()) {
                        Property existingProperty = existingPropertyOptional.get();
                        Set<PaymentAccount> paymentAccounts = existingProperty.getPaymentAccounts();
                        PaymentAccount paymentAccount = PaymentAccount.builder()
                                .shortCode(rowData.get("shortcode"))
                                .accountNumber(rowData.get("account_number"))
                                .accountType(PaymentAccountType.valueOf(rowData.get("account_type")))
                                .billType(ApartmentBillType.valueOf(rowData.get("purpose")))
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
                                .name(rowData.get("property_name"))
                                .landlord(landlord)
                                .build();

                        property = propertyRepository.save(property);
                        LOGGER.info("Create new Property: success");

                        Set<PaymentAccount> paymentAccounts = property.getPaymentAccounts();
                        PaymentAccount paymentAccount = PaymentAccount.builder()
                                .shortCode(rowData.get("shortcode"))
                                .accountNumber(rowData.get("account_number"))
                                .accountType(PaymentAccountType.valueOf(rowData.get("account_type")))
                                .billType(ApartmentBillType.valueOf(rowData.get("purpose")))
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

            XSSFSheet sheet2 = wb.getSheetAt(1);

            // Process the header row
            Row sheet2HeaderRow = sheet2.getRow(0);
            List<String> sheet2Headers = new ArrayList<>();
            for (Cell cell : sheet2HeaderRow) {
                sheet2Headers.add(cell.getStringCellValue());
            }

            // Process the rest of the rows
            Iterator<Row> sheet2RowIterator = sheet2.iterator();
            sheet2RowIterator.next(); // Skip the header row
            while (sheet2RowIterator.hasNext()) {
                LOGGER.info("Begin Sheet 2 processing");
                Row row = sheet2RowIterator.next();
                Map<String, String> rowData = new HashMap<>();
                for (int i = 0; i < sheet2Headers.size(); i++) {
                    Cell cell = row.getCell(i);
                    if (cell != null) {
                        String columnHeader = sheet2Headers.get(i);
                        if (columnHeader != null) {
                            String cellValue = cell.getStringCellValue().trim(); // Trim whitespaces
                            rowData.put(columnHeader, cellValue);
                        }
                    }
                }

                String propertyName = rowData.get("property_name");
                String blockName = rowData.get("block");
                String floorName = rowData.get("floor");
                String apartmentName = rowData.get("apartment");
                String tenantName = rowData.get("tenant_name");
                String idNumber = rowData.get("id_number");
                String phoneNumber = rowData.get("phone_number");
                String otherPhoneNumbers = rowData.get("other_phone_numbers");
                String moveInDate = rowData.get("move_in_date");
                String rent = rowData.get("rent");
                String garbage = rowData.get("garbage");
                String serviceCharge = rowData.get("service_charge");

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

                                continue;
                            } else {
                                LOGGER.info("Apartment not found");
                                Apartment apartment = Apartment.builder()
                                        .name(apartmentName)
                                        .floor(floor)
                                        .build();
                                apartment = apartmentRepository.save(apartment);
                                Set<ApartmentBill> apartmentBills = apartment.getApartmentBills();
//                                check if rent is null before doing this,also check if bill type exists
                                if (rent != null) {
                                    ApartmentBill rentBill = ApartmentBill.builder()
                                            .billType(ApartmentBillType.rent)
                                            .billAmount(Long.parseLong(rent))
                                            .apartment(apartment)
                                            .fixed(true)
                                            .build();
                                    apartmentBills.add(rentBill);
                                }

//                                check if garbage is null before doing this
                                if (garbage != null) {
                                    ApartmentBill garbageBill = ApartmentBill.builder()
                                            .billType(ApartmentBillType.garbage)
                                            .billAmount(Long.parseLong(garbage))
                                            .apartment(apartment)
                                            .fixed(true)
                                            .build();
                                    apartmentBills.add(garbageBill);
                                }

//                                check if serviceCharge is null before doing this
                                if (serviceCharge != null) {
                                    ApartmentBill serviceChargeBill = ApartmentBill.builder()
                                            .billType(ApartmentBillType.service_charge)
                                            .billAmount(Long.parseLong(serviceCharge))
                                            .apartment(apartment)
                                            .fixed(true)
                                            .build();
                                    apartmentBills.add(serviceChargeBill);
                                }
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
//                                check apartment bills

                                continue;
                            } else {
                                LOGGER.info("Apartment not found");
                                Apartment apartment = Apartment.builder()
                                        .name(apartmentName)
                                        .floor(floor)
                                        .build();
                                apartment = apartmentRepository.save(apartment);
                                Set<ApartmentBill> apartmentBills = apartment.getApartmentBills();
//                                check if rent is null before doing this,also check if bill type exists
                                if (rent != null) {
                                    ApartmentBill rentBill = ApartmentBill.builder()
                                            .billType(ApartmentBillType.rent)
                                            .billAmount(Long.parseLong(rent))
                                            .apartment(apartment)
                                            .fixed(true)
                                            .build();
                                    apartmentBills.add(rentBill);
                                }

//                                check if garbage is null before doing this
                                if (garbage != null) {
                                    ApartmentBill garbageBill = ApartmentBill.builder()
                                            .billType(ApartmentBillType.garbage)
                                            .billAmount(Long.parseLong(garbage))
                                            .apartment(apartment)
                                            .fixed(true)
                                            .build();
                                    apartmentBills.add(garbageBill);
                                }

//                                check if serviceCharge is null before doing this
                                if (serviceCharge != null) {
                                    ApartmentBill serviceChargeBill = ApartmentBill.builder()
                                            .billType(ApartmentBillType.service_charge)
                                            .billAmount(Long.parseLong(serviceCharge))
                                            .apartment(apartment)
                                            .fixed(true)
                                            .build();
                                    apartmentBills.add(serviceChargeBill);
                                }
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
                        if (rent != null) {
                            ApartmentBill rentBill = ApartmentBill.builder()
                                    .billType(ApartmentBillType.rent)
                                    .billAmount(Long.parseLong(rent))
                                    .apartment(apartment)
                                    .fixed(true)
                                    .build();
                            apartmentBills.add(rentBill);
                        }
                        if (garbage != null) {
                            ApartmentBill garbageBill = ApartmentBill.builder()
                                    .billType(ApartmentBillType.garbage)
                                    .billAmount(Long.parseLong(garbage))
                                    .apartment(apartment)
                                    .fixed(true)
                                    .build();
                            apartmentBills.add(garbageBill);
                        }
                        if (serviceCharge != null) {
                            ApartmentBill serviceChargeBill = ApartmentBill.builder()
                                    .billType(ApartmentBillType.service_charge)
                                    .billAmount(Long.parseLong(serviceCharge))
                                    .apartment(apartment)
                                    .fixed(true)
                                    .build();
                            apartmentBills.add(serviceChargeBill);
                        }
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

                Map<String, Object> response = responseHandler.generateResponse("success", properties, null);
                return ResponseEntity.status(HttpStatus.OK).body(response);

            }

            LOGGER.info("Create Property: Complete");

            Map<String, Object> response = responseHandler.generateResponse("fail", null, "Password 1 does not match password 2");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            LOGGER.error("Error creating property: {}", e.getMessage());
            Map<String, Object> errorResponse = responseHandler.generateResponse("fail", null, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
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

    private void processSheet(XSSFSheet sheet, AuthenticationResponse token, Set<Property> properties, List<Tenant> tenants) {
        // Process the header row
        Row headerRow = sheet.getRow(0);
        List<String> headers = new ArrayList<>();
        for (Cell cell : headerRow) {
            headers.add(cell.getStringCellValue());
        }

        // Process the rest of the rows
        Iterator<Row> rowIterator = sheet.iterator();
        rowIterator.next(); // Skip the header row
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Map<String, String> rowData = extractRowData(row, headers);
            processRowData(rowData, token, properties, tenants);
        }
    }

    private Map<String, String> extractRowData(Row row, List<String> headers) {
        Map<String, String> rowData = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null) {
                String columnHeader = headers.get(i);
                if (columnHeader != null) {
                    String cellValue = cell.getStringCellValue().trim(); // Trim whitespaces
                    rowData.put(columnHeader, cellValue);
                }
            }
        }
        return rowData;
    }

    private void processRowData(Map<String, String> rowData, AuthenticationResponse token, Set<Property> properties, List<Tenant> tenants) {
        // Your logic to process rowData, including creating or updating Property, Block, Floor, Apartment, and Tenant entities
        // This method should contain the logic that was previously duplicated in your original code
    }




}
