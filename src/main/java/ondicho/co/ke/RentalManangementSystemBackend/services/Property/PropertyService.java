package ondicho.co.ke.RentalManangementSystemBackend.services.Property;

import ondicho.co.ke.RentalManangementSystemBackend.models.Property.*;
import ondicho.co.ke.RentalManangementSystemBackend.repositories.Property.*;
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

    //    Create property to-do:
//    1.Check if landlord exists and is active
//    2.Create property
//    3.Register property block=>floors=>apartments=>tenants=>apartmentBills
//    4.Register payment accounts and register urls
//    5.
    public ResponseEntity<Map<String, Object>> createProperty(MultipartFile file) {
        try {
            LOGGER.info("Create Property: Start");
            LOGGER.info("Beginning Excel data processing");
            XSSFWorkbook wb = new XSSFWorkbook(file.getInputStream());
            XSSFSheet ws = wb.getSheetAt(0); // Assuming you want the first sheet

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
                Map<String, String> rowData = new HashMap<>();
                Row row = rowIterator.next();
                for (int i = 0; i < headers.size(); i++) {
                    Cell cell = row.getCell(i);
                    if (cell != null) {
                        String columnHeader = headers.get(i);
                        if (columnHeader != null) {
                            String cellValue = getStringCellValue(cell).trim(); // Trim whitespaces
                            rowData.put(columnHeader, cellValue);
                        }

//                        crunch row data
                    }
                }
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
