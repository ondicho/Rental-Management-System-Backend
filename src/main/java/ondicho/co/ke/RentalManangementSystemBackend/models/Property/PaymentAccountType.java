package ondicho.co.ke.RentalManangementSystemBackend.models.Property;

public enum PaymentAccountType {
    TILL_NUMBER("Buy Goods"),
    PAY_BILL("Pay Bill"),
    BANK("Bank Account");

    private final String description;

    PaymentAccountType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static PaymentAccountType getByName(String name) {
        for (PaymentAccountType type : values()) {
            if (type.getDescription().equals(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException(name + " is not a valid PaymentAccountType");
    }
}
