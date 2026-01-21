package org.example.webapplication.exception;

public enum ErrorCode {
    USER_EXISTED(1001,"User already exists"),
    DRIVER_NOT_FOUND(1002,"User not found"),
    USER_ROLE_NOT_FOUND(1003,"User role not found"),
    TRUCK_NOT_FOUND(1004,"Truck not found"),
    LICENCE_PLATE_EXISTED(1004,"License Plate already exists"),
    TRAVEL_NOT_FOUND(1005,"Travel not found"),
    SCHEDULE_NOT_FOUND(1006,"Schedule not found"),
    INVALID_APPROVAL_TRANSITION(1006,"Invalid approval transition"),
    START_DATE_AND_END_DATE_NOT_NULL(1007,"Start date and end date not null"),
    END_DATE_GREATER_OR_EQUAL_START_DATE(1008,"End date greater or equal start date"),
    EXPENSE_NOT_FOUND(1009,"Expense not found"),

    ROLE_NOT_FOUND(2001,"Role not found"),
    ROLE_EXISTED(2002,"Role already exists"),
    FILE_EMPTY(2003,"File is empty"),
    FILE_TOO_LARGE(2004,"File is too large"),

    INVOICE_NOT_FOUND(3001,"Invoice not found"),
    ITEM_NOT_FOUND(3002,"Item not found"),
    USER_NOT_FOUND(3003,"User not found"),
    INVENTORY_NOT_FOUND(3004,"Inventory not found"),

    JWT_NOT_CREATED(4000,"Cannot create JWT"),
    INVALID_SIGNATURE(4001,"Invalid signature"),
    INVALID_TOKEN(4002,"Invalid token"),
    MISSING_TOKEN(4003,"Missing token"),
    INVALID_PASSWORD(4004,"Invalid password"),
    FORBIDDEN(4005,"You not have permission to perform this operation"),

    UNAUTHENTICATED(5000,"Unauthenticated "),//ko dang nhap
    ACCESS_DENIED(5001,"You do not have permission to access this resource"),
    INVALID_PERMISSIONS(5003,"Cannot parse permissions from token"),
    PERMISSION_NOT_FOUND(5004,"Permission not found"),

    TRUCK_NOT_ACTIVE(6001,"Truck not active"),
    TRUCK_ALREADY_IN_TRAVEL_TODAY(6002,"Truck already in travel today"),
    INVALID_REQUEST_DATA(6003,"Invalid request data"),
    TRUCK_ALREADY_IN_TRAVEL_SAME_DAY(6004,"Truck already in travel same day"),
    TRUCK_HAS_TRAVEL_TODAY(6005,"Truck has travel today"),
    //VALIDATION
    USERNAME_NOT_NULL(7001,"username must be not null"),
    USERNAME_NOT_VALID(7002,"username must be at least 3 charactors"),
    PASSWORD_NOT_NULL(7003,"password not null"),
    PASSWORD_NOT_VALID(7004,"password must be at least 5 charactors"),
    ID_NOT_FOUND(7005,"ID not found"),
    DATE_IN_PAST(7006,"birthday must be in the past"),
    PHONE_NOT_VALID(7007,"phone number invalid"),

    EXPENSE_GREATER_THAN_ZERO(7007,"expense must be greater than 0"),
    QUANTITY_GREATER_THAN_ZERO(7013,"quantity must be greater than 0"),
    ADVANCE_GREATER_THAN_ZERO(7018,"advance must be greater than 0"),

    DESCRIPTION_NOT_EXCEED_300(7008,"description must not exceed 300 characters"),

    TYPE_EXPENSE_NOT_NULL(7006,"Type expense must be not null"),
    TRAVEL_ID_NOT_NULL(7009,"travelId must be not null"),
    ITEM_ID_NOT_NULL(7012,"itemId must be not null"),
    DRIVER_ID_NOT_NULL(7016,"driverId must be not null"),
    TRUCK_ID_NOT_NULL(7017,"truckId must be not null"),
    SCHEDULE_ID_NOT_NULL(7018,"scheduleId must be not null"),
    USER_ID_NOT_NULL(7019,"userId must be not null"),

    INVOICE_ID_NOT_NULL(7014,"invoiceId must be not null"),
    CUSTOMER_NAME_NOT_NULL(7015,"customerName must be not null"),

    DATE_NOT_IN_FUTURE(7011,"incurredDate cannot be in the future"),
    DATE_NOT_NULL(7017,"date must be not null"),
    DATE_MUST_BE_TODAY_OR_FUTURE(7018,"Date must be today or in the future"),
    PLACE_NOT_NULL(7019,"place must be not null"),





    NOT_ENOUGH_STOCK(8000,"Not enough stock"),
    EXCEL_IMPORT_FAILED(8001,"Excel import failed" ),
    DRIVER_ALREADY_ASSIGNED(8002,"Driver already assigned"),
    INVALID_LICENSE_PLATE(8003, "Liscense plate is invalid"),
    INVALID_TRUCK_STATUS(8004, "Invalid truck status"),

    TRUCK_ALREADY_IN_TRAVEL(8005,"Truck already exists"),
    START_END_PLACE_MUST_DIFFERENT(8006,"Start and end place must different"),
    INVALID_EXPENSE(8007,"Invalid expense"),
    SCHEDULE_ROUTE_EXISTED(8008,"Schedule route already exists"),
    TRAVEL_LOCKED_BY_EXPENSE(8009,"Travel locked by expense"),
    SCHEDULE_ALREADY_ASSIGNED(8010,"Schedule already assigned"),

    VALIDATION_FAILED(102, "VALIDATION_FAILED"),
    TYPE_TRUCK_REQUIRED(103, "TYPE_TRUCK_REQUIRED"),
    LICENSE_PLATE_REQUIRED(104, "LICENSE_PLATE_REQUIRED"),
    LICENSE_PLATE_INVALID(105, "LICENSE_PLATE_INVALID"),
    TRUCK_STATUS_REQUIRED(106, "truck status required"),
    GANMOOC_REQUIRED(107, "GANMOOC_REQUIRED"),

    INVALID_INVENTORY_STATUS(108, "Invalid inventory status"),
    ;


    ErrorCode(int code ,String message){
        this.code = code;
        this.message = message;
    }
    private final int code;
    private final String message;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}