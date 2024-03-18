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
            List<Property> properties = new ArrayList<>();
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
                            paymentAccounts.add(paymentAccount);

                            existingProperty.setPaymentAccounts(paymentAccounts);

                            propertyRepository.save(existingProperty);
                        } else {
                            Property property = Property.builder()
                                    .name(rowData.get("property_name"))
                                    .landlord(landlord)
                                    .build();

                            property = propertyRepository.save(property);

                            Set<PaymentAccount> paymentAccounts = property.getPaymentAccounts();
                            PaymentAccount paymentAccount = PaymentAccount.builder()
                                    .shortCode(rowData.get("shortcode"))
                                    .accountNumber(rowData.get("account_number"))
                                    .accountType(PaymentAccountType.valueOf(rowData.get("account_type")))
                                    .billType(ApartmentBillType.valueOf(rowData.get("purpose")))
                                    .property(property)
                                    .build();
                            paymentAccounts.add(paymentAccount);

                            property.setPaymentAccounts(paymentAccounts);

                            property = propertyRepository.save(property);

                            properties.add(property);
                        }
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

                String propertyName=rowData.get("property_name");
                String blockName=rowData.get("property_name");
                String floorName=rowData.get("property_name");
                String apartmentName=rowData.get("property_name");
                String tenantName=rowData.get("property_name");
                String idNumber=rowData.get("property_name");
                String phoneNumber=rowData.get("property_name");
                String otherPhoneNumbers=rowData.get("property_name");
                String moveInDate=rowData.get("property_name");
                String rent=rowData.get("property_name");
                String garbage=rowData.get("property_name");
                String serviceCharge=rowData.get("property_name");



            }
//            }

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


    public Block registerBlock(Block block) {
        try {
            LOGGER.info("Register Block: Start");
            Set<Floor> savedFloors = new HashSet<Floor>();
            for (Floor floor : block.getFloors()) {
                Floor savedFloor = registerFloor(floor);
                savedFloors.add(savedFloor);
            }
            block.setFloors(savedFloors);
            return blockRepository.save(block);
        } catch (Exception e) {
            LOGGER.error("Error registering block: {}", e.getMessage());
        }
        return null;
    }

    public Floor registerFloor(Floor floor) {
        try {
            LOGGER.info("Register Floor: {}", floor.getName());
            Set<Apartment> apartments = new HashSet<Apartment>();
            for (Apartment apartment : floor.getApartments()) {
                Apartment savedApartment = registerApartment(apartment);
                apartments.add(savedApartment);
            }
            floor.setApartments(apartments);
            return floorRepository.save(floor);
        } catch (Exception e) {
            LOGGER.error("Error registering floor: {}", e.getMessage());
        }
        return null;
    }

    //    register fixed apartment bills eg rent
//    register tenant
//    edit tenant feature later for when a tenant moves/vacates
    public Apartment registerApartment(Apartment apartment) {
        LOGGER.info("Register Apartment: {}", apartment.getName() + " of floor: " + apartment.getFloor().getName());
        try {
            Set<ApartmentBill> fixedBills = new HashSet<ApartmentBill>();
            Tenant tenant = registerTenant(apartment.getTenant());
            for (ApartmentBill bill : apartment.getApartmentBills()) {
                ApartmentBill savedBill = registerApartmentBill(bill);
                fixedBills.add(savedBill);
            }
            apartment.setApartmentBills(fixedBills);
            apartment.setTenant(tenant);

            return apartmentRepository.save(apartment);
        } catch (Exception e) {
            LOGGER.error("Error registering apartment: {}", e.getMessage());
        }
        return null;
    }

    public ApartmentBill registerApartmentBill(ApartmentBill apartmentBill) {
        try {
            LOGGER.info("Register Apartment bill: {}", apartmentBill.getBillType() + " of apartment: " + apartmentBill.getApartment().getName());
            return apartmentBillRepository.save(apartmentBill);
        } catch (Exception e) {
            LOGGER.error("Error registering apartment: {}", e.getMessage());
        }
        return null;
    }


    public Tenant registerTenant(Tenant tenant) {
        try {
            LOGGER.info("Register Tenant: {}", tenant.getName() + " of apartment: " + tenant.getApartment().getName());
            return tenantRepository.save(tenant);
        } catch (Exception e) {
            LOGGER.error("Error registering tenant: {}", e.getMessage());
        }
        return null;
    }

    public PaymentAccount registerPaymentAccount(PaymentAccount paymentAccount) {
        try {
            LOGGER.info("Register PaymentAccount", paymentAccount.getShortCode() + " of apartment: " + paymentAccount.getProperty().getName());
            return paymentAccountRepository.save(paymentAccount);
        } catch (Exception e) {
            LOGGER.error("Error registering PaymentAccount: {}", e.getMessage());
        }
        return null;
    }


}
