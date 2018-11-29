package utils;

public final class Encryption {

  public static String encryptDecryptXOR(String rawString) {

    // If encryption is enabled in Config.
    if (Config.getEncryption()) {

      // TODO: Create a more complex code and store it somewhere better : FIX
      //The key is stored in config.json
      //Stored encryptionKey in config.json as a string and parsing it to a char array
      char [] encryptionKey = Config.getEncryptionKey().toCharArray();


      // Stringbuilder enables you to play around with strings and make useful stuff
      StringBuilder thisIsEncrypted = new StringBuilder();

      // TODO: This is where the magic of XOR is happening. Are you able to explain what is going on? : FIX
      //Using for loop to go through rawString (the json data).
      for (int i = 0; i < rawString.length(); i++) {
        //We look at i and take the binary value of that char from rawString
        //and doing exclusive or with the binary value of a char of the result (i% the length of the encryptionKey)
        //then we'll have a new binary value that is appended to the stringbuilder.
        //The reason we use modulus % is because we don't want to exceed the length of the encryptionKey char array.
        thisIsEncrypted.append((char) (rawString.charAt(i) ^ encryptionKey[i % encryptionKey.length]));
      }

      // We return the encrypted string
      return thisIsEncrypted.toString();

    } else {
      // We return without having done anything
      return rawString;
    }
  }
}
