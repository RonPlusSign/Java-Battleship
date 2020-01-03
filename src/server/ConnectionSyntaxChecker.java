package server;

public class ConnectionSyntaxChecker {

    /**
     * Function that checks if the argument is a valid ip (format: 127.0.0.1)
     *
     * @param ip the string that should contain the ip address
     * @return true if the argument is a valid ip address, false otherwise
     */
    public static boolean validIP(String ip) {
        try {
            if (ip == null || ip.isEmpty()) {
                return false;
            }

            //divide the ip in parts
            String[] parts = ip.split("\\.");
            if (parts.length != 4) {
                return false;
            }

            //check if each part has a valid value (from 0 to 255)
            for (String s : parts) {
                int i = Integer.parseInt(s);
                if ((i < 0) || (i > 255)) {
                    return false;
                }
            }

            //lastly, check if the ip string doesn't end with a dot. ( "127.0.0.1." isn't a valid ip address)
            return !ip.endsWith(".");

        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    /**
     * Function that checks if the argument is a valid port (from 1 to 65535)
     *
     * @param port the string that should contain the port value
     * @return true if the argument is a valid port, false otherwise
     */
    public static boolean validPort(String port) {
        try {
            if (port == null || port.isEmpty()) return false;

            int i = Integer.parseInt(port); //try to convert from String to int
            return (i >= 1) && (i <= 65535);    //check if the number is a valid port
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

}
