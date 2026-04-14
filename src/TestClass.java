class Car {
    public enum CarType {
        SEDAN,
        SUV,
        COUPE,
        PICKUP
    };

    public static String compareStringWithEnum(){
        String outcome;
        if (CarType.SEDAN.toString().equalsIgnoreCase("SeDaN")){
//            if (CarType.SEDAN.toString().equalsIgnoreCase("sedan")){
              outcome = "The both values are the same despite case.";
              System.out.println(CarType.PICKUP);
          }
          else {
              outcome = "There is some difference. It is counting same word as same";
          }
          return outcome;
    };

    public static boolean doesContainSymbol(){
        boolean outcome;
        String date = "12/24/2024";
        outcome = date.contains("/");
        return outcome;
    }
}



void main() {
//    System.out.println(Car.compareStringWithEnum());
    System.out.println(Car.doesContainSymbol());
}