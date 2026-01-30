package org.example.webapplication.exception;

/*1000 - 1999: Hệ thống & Xác thực (Auth, JWT, Permissions).

2000 - 2999: Quản lý Người dùng & Vai trò (User, Driver, Role).

3000 - 3999: Quản lý Xe & Lịch trình (Truck, Schedule, Travel).

4000 - 4999: Tài chính & Kho (Invoice, Expense, Inventory).

9000+: Lỗi Validation chung (Dữ liệu đầu vào).*/


public enum ErrorCode {

    JWT_NOT_CREATED(1000, "auth.jwt_create_failed"),
    INVALID_SIGNATURE(1001, "auth.jwt_invalid_signature"),
    INVALID_TOKEN(1002, "auth.jwt_invalid_token"),
    MISSING_TOKEN(1003, "auth.jwt_missing_token"),
    INVALID_PERMISSIONS(1004, "auth.jwt_invalid_permissions"),

    UNAUTHENTICATED(1100, "auth.unauthenticated"),
    INVALID_PASSWORD(1101, "auth.invalid_password"),
    USER_NOT_LOGGED_IN(1102, "auth.not_logged_in"),

    UNAUTHORIZED(1200, "auth.unauthorized"),
    FORBIDDEN(1201, "auth.forbidden"),
    ACCESS_DENIED(1202, "auth.access_denied"),
    PERMISSION_NOT_FOUND(1203, "auth.permission_not_found"),

    USER_NOT_FOUND(2000, "user.not_found"),
    USER_EXISTED(2001, "user.existed"),
    USERNAME_ALREADY_EXISTS(2002, "user.username_exists"),
    USER_ID_NOT_NULL(2003, "user.id_not_null"),

    DRIVER_NOT_FOUND(2100, "driver.not_found"),
    DRIVER_ALREADY_ASSIGNED(2101, "driver.already_assigned"),

    ROLE_NOT_FOUND(2200, "role.not_found"),
    ROLE_EXISTED(2201, "role.existed"),
    ROLE_NAME_ALREADY_EXISTS(2202, "role.name_exists"),

    USER_ROLE_NOT_FOUND(2300, "user_role.not_found"),
    USER_ROLE_ALREADY_ASSIGNED(2301, "user_role.already_assigned"),

    TRUCK_NOT_FOUND(3000, "truck.not_found"),
    TRUCK_NOT_ACTIVE(3001, "truck.not_active"),
    INVALID_TRUCK_STATUS(3002, "truck.invalid_status"),

    LICENCE_PLATE_EXISTED(3010, "truck.license_plate_existed"),
    INVALID_LICENSE_PLATE(3011, "truck.invalid_license_plate"),

    TRAVEL_NOT_FOUND(3100, "travel.not_found"),
    TRUCK_ALREADY_IN_TRAVEL_TODAY(3101, "travel.truck_already_today"),
    TRUCK_ALREADY_IN_TRAVEL_SAME_DAY(3102, "travel.truck_already_same_day"),
    TRUCK_HAS_TRAVEL_TODAY(3103, "travel.truck_has_today"),
    TRUCK_ALREADY_IN_TRAVEL(3104, "travel.truck_already_in_travel"),

    TRAVEL_LOCKED_BY_EXPENSE(3110, "travel.locked_by_expense"),

    SCHEDULE_NOT_FOUND(3200, "schedule.not_found"),
    SCHEDULE_ROUTE_EXISTED(3201, "schedule.route_existed"),
    SCHEDULE_ALREADY_ASSIGNED(3202, "schedule.already_assigned"),
    SCHEDULE_ALREADY_IN_THIS_STATUS(3203, "schedule.already_in_this_status"),

    START_END_PLACE_MUST_DIFFERENT(3300, "business.start_end_place_must_different"),
    INVALID_APPROVAL_TRANSITION(3301, "business.invalid_approval_transition"),

    EXPENSE_NOT_FOUND(4000, "expense.not_found"),
    INVALID_EXPENSE(4001, "expense.invalid"),
    EXPENSE_NOT_APPROVED(4002, "expense.not_approved"),
    EXPENSE_ALREADY_APPROVED(4003, "expense.already_approved"),

    INVOICE_NOT_FOUND(4100, "invoice.not_found"),
    INVOICE_ALREADY_PAID(4101, "invoice.already_paid"),
    INVOICE_NOT_PAID(4102, "invoice.not_paid"),
    INVOICE_CANNOT_BE_DELETED(4103, "invoice.cannot_be_deleted"),

    INVENTORY_NOT_FOUND(4200, "inventory.not_found"),
    INVALID_INVENTORY_STATUS(4201, "inventory.invalid_status"),
    NOT_ENOUGH_STOCK(4202, "inventory.not_enough_stock"),

    ITEM_NOT_FOUND(4300, "item.not_found"),
    INVALID_QUANTITY(4301, "item.invalid_quantity"),
    EXCEL_IMPORT_FAILED(4302, "item.excel_import_failed"),

    PAYROLL_NOT_FOUND(4400, "payroll.not_found"),
    PAYROLL_ALREADY_CLOSED(4401, "payroll.already_closed"),

    VALIDATION_FAILED(9000, "validation.failed"),
    INVALID_REQUEST_DATA(9001, "validation.invalid_request"),

    USERNAME_NOT_NULL(9100, "validation.username.not_null"),
    USERNAME_NOT_VALID(9101, "validation.username.invalid"),

    PASSWORD_NOT_NULL(9110, "validation.password.not_null"),
    PASSWORD_NOT_VALID(9111, "validation.password.invalid"),

    DESCRIPTION_NOT_EXCEED_300(9120, "validation.description.exceed_300"),
    PHONE_NOT_VALID(9130, "validation.phone.invalid"),
    CUSTOMER_NAME_NOT_NULL(9140, "validation.customer_name.not_null"),

    EXPENSE_GREATER_THAN_ZERO(9200, "validation.expense.gt_zero"),
    QUANTITY_GREATER_THAN_ZERO(9201, "validation.quantity.gt_zero"),
    ADVANCE_GREATER_THAN_ZERO(9202, "validation.advance.gt_zero"),

    DATE_NOT_NULL(9300, "validation.date.not_null"),
    DATE_IN_PAST(9301, "validation.date.in_past"),
    DATE_NOT_IN_FUTURE(9302, "validation.date.not_in_future"),
    DATE_MUST_BE_TODAY_OR_FUTURE(9303, "validation.date.today_or_future"),

    START_DATE_AND_END_DATE_NOT_NULL(9310, "validation.date_range.not_null"),
    END_DATE_GREATER_OR_EQUAL_START_DATE(9311, "validation.date_range.invalid"),

    DRIVER_ID_NOT_NULL(9400, "validation.driver_id.not_null"),
    TRUCK_ID_NOT_NULL(9401, "validation.truck_id.not_null"),
    TRAVEL_ID_NOT_NULL(9402, "validation.travel_id.not_null"),
    SCHEDULE_ID_NOT_NULL(9403, "validation.schedule_id.not_null"),
    ITEM_ID_NOT_NULL(9404, "validation.item_id.not_null"),
    INVOICE_ID_NOT_NULL(9405, "validation.invoice_id.not_null"),
    ID_NOT_FOUND(9406, "validation.id.not_found"),

    TYPE_EXPENSE_NOT_NULL(9500, "validation.expense_type.not_null"),
    PLACE_NOT_NULL(9501, "validation.place.not_null"),
    TYPE_TRUCK_REQUIRED(9502, "validation.truck_type.required"),
    LICENSE_PLATE_REQUIRED(9503, "validation.license_plate.required"),
    LICENSE_PLATE_FORMAT_INVALID(9504, "validation.license_plate.format_invalid"),
    TRUCK_STATUS_REQUIRED(9505, "validation.truck_status.required"),
    GANMOOC_REQUIRED(9506, "validation.ganmooc.required"),
    LICENSE_PLATE_INVALID(9507, "validation.license_plate.invalid"),

    FILE_EMPTY(9600, "validation.file.empty"),
    FILE_TOO_LARGE(9601, "validation.file.too_large"),

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