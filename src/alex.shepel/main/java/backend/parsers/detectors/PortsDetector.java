package backend.parsers.detectors;

import java.util.ArrayList;
import java.util.HashMap;

/*
 * File: PortsDetector.java
 * -----------------------------------------------
 * Looks for ports in the parsed module.
 */
public class PortsDetector {

    /* List of found ports (inputs and outputs). */
    private final HashMap<String, PortDescriptor> inputs = new HashMap<>();
    private final HashMap<String, PortDescriptor> outputs = new HashMap<>();

    /**
     * The class constructor.
     * Creates a HashMap from a found ports descriptions.
     *
     * @param parsedFile The ArrayList object
     *                   that contains all code lines
     *                   of parsed file.
     */
    public PortsDetector(ArrayList<String> parsedFile) {
        for (String codeLine: parsedFile)
            if (isPortDeclaration(codeLine))
                parsePorts(codeLine);
    }

    /**
     * Checks if ongoing line of code contains input declaration.
     *
     * @param codeLine The String value that contains
     *                 a separate line of code.
     * @return The boolean "true" value, if ongoing code line
     *         contains an input declaration. Else "false".
     */
    private boolean isPortDeclaration(final String codeLine) {
        final String cleanLine = deleteSpacings(codeLine);
        boolean isComment = cleanLine.startsWith("//") || cleanLine.startsWith("*/");
        boolean isInputOutputFound = cleanLine.contains("input") || cleanLine.contains("output");
        boolean isTypeFound = cleanLine.contains("logic") || cleanLine.contains("wire") || cleanLine.contains("reg");
        return !isComment && isInputOutputFound && isTypeFound;
    }

    /**
     * Returns all ports that are written in the ongoing line of code.
     *
     * @param portDeclaration The String value that contains
     *                        a separate line of code with
     *                        the port(-s) declaration.
     */
    private void parsePorts(final String portDeclaration) {
        final PortDescriptor descriptor = new PortDescriptor();
        String cleanDeclaration = deleteSpacings(portDeclaration);

        final boolean isInput = portDeclaration.contains("input");
        cleanDeclaration = cleanDeclaration.replace("input", "").replace("output", "");

        descriptor.setType(getPortType(cleanDeclaration));
        cleanDeclaration = cleanDeclaration.replace(descriptor.getType(), "");

        descriptor.setSigned(getSigned(cleanDeclaration));
        cleanDeclaration = cleanDeclaration.replace(descriptor.getSigned(), "");

        descriptor.setPackedSize(getPackedSize(cleanDeclaration));
        cleanDeclaration = cleanDeclaration.replace(descriptor.getPackedSize(), "");

        descriptor.setUnpackedSize(getUnpackedSize(cleanDeclaration));
        cleanDeclaration = cleanDeclaration.replace(descriptor.getUnpackedSize(), "");

        for (final String name: cleanDeclaration.split(",")) {
            final PortDescriptor copy = descriptor.deepCopy();
            copy.setName(name);

            if (isInput)
                inputs.put(name, copy);
            else
                outputs.put(name, copy);
        }
    }

    /**
     * Deletes all spacings from a specified line of code
     * for further processing.
     *
     * @param codeLine The String value that contains
     *                 a separate line of code.
     * @return The modified line of code without spacings.
     */
    private String deleteSpacings(String codeLine) {
        String[] spacingSymbols = {" ", "\t", "\n"};

        for (String symbol: spacingSymbols)
            codeLine = codeLine.replace(symbol, "");

        return codeLine;
    }

    /**
     * Returns a type of a specified port.
     *
     * @param portDeclaration The String value that contains
     *                        a separate line of code with
     *                        the declaration of port.
     * @return The string value of a port's type.
     *         If port does not have any type,
     *         returns a "logic" type that is used by default.
     */
    private String getPortType(String portDeclaration) {
        String[] portTypes = {"logic", "wire", "reg"};

        for (String portType: portTypes)
            if (portDeclaration.contains(portType))
                return portType;

        return "";
    }

    /**
     * Returns a "signed" word if specified port is signed.
     *
     * @param codeLine The String value that contains
     *                 a separate line of code.
     * @return "signed" word if specified port is signed.
     *         If port is not signed, returns empty string.
     */
    private String getSigned(String codeLine) {
        if (codeLine.contains("signed"))
            return "signed";

        return "";
    }

    /**
     * Returns a packed size of a specified port.
     *
     * @param codeLine The String value that contains
     *                 a separate line of code.
     * @return The string value of packed size of a port.
     *         If port is not packed, returns empty string.
     */
    private String getPackedSize(String codeLine) {
        final int lastInd = codeLine.contains(",") ? codeLine.length() - 2 : codeLine.length() - 1;

        if (codeLine.contains("[") && codeLine.indexOf("]") < lastInd)
            return codeLine.substring(codeLine.indexOf('['), codeLine.indexOf(']') + 1);

        return "";
    }

    /**
     * Returns an unpacked size of a specified port.
     *
     * @param codeLine The String value that contains
     *                 a separate line of code.
     * @return The string value of unpacked size of a port.
     *         If port is not unpacked, returns empty string.
     */
    private String getUnpackedSize(String codeLine) {
        if (codeLine.contains("["))
            return codeLine.substring(codeLine.indexOf('['), codeLine.indexOf(']') + 1);

        return "";
    }

    /**
     * Returns all input ports that was found.
     *
     * @return The HashMap object.
     *         Key contains a name of a found port.
     *         Value stores a full information about port
     *         as PortDescriptor object.
     */
    public HashMap<String, PortDescriptor> getInputPorts() {
        return inputs;
    }

    /**
     * Returns all input ports that was found.
     *
     * @return The HashMap object.
     *         Key contains a name of a found port.
     *         Value stores a full information about port
     *         as PortDescriptor object.
     */
    public HashMap<String, PortDescriptor> getOutputPorts() {
        return outputs;
    }

}
