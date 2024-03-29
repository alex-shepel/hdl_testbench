package backend.filesWriter.codegens.sv;

import backend.BackendParameters;
import backend.parsers.detectors.PortDescriptor;

import java.io.IOException;
import java.util.HashMap;

/*
 * File: ReadDriverCodeGenerator.java
 * -----------------------------------------------
 * Inherits CodeGenerator object.
 * Overwrites "BackendParameters.READ_DRIVER_SV" file
 * based on specified data.
 */
public class CheckerCodegen extends SVCodegen implements BackendParameters {

    private static final String[] MISMATCH_INIT = {
            "\t\tiface.<port_name>_errors = 0;",
    };

    private static final String[] MISMATCH_INIT_UNPACKED = {
            "\t\tfor (int i = 0; i <= PARAMETER - 1; i++) begin",
            "\t\t   iface.<port_name>_errors[i] = 0;",
            "\t\tend",
    };

    private static final String[] MISMATCH = {
            "\t\tisEqual = (iface.<port_name> == iface.<port_name>_expect);",
            "\t\tisDefined = (iface.<port_name>_expect !== 'x);",
            "\t\tiface.<port_name>_mismatch = ~isEqual && isDefined;",
    };

    private static final String[] MISMATCH_UNPACKED = {
            "\t\tfor (int i = 0; i <= PARAMETER - 1; i++) begin",
            "\t\t   isEqual = (iface.<port_name>[i] == iface.<port_name>_expect[i]);",
            "\t\t   isDefined = (iface.<port_name>_expect[i] !== 'x);",
            "\t\t   iface.<port_name>_mismatch[i] = ~isEqual && isDefined;",
            "\t\tend",
    };

    private static final String[] MISMATCH_COUNT = {
            "\t\tif (iface.<port_name>_mismatch) begin",
            "\t\t   iface.<port_name>_errors++;",
            "\t\t   iface.test_passed = 0;",
            "\t\tend",
    };

    private static final String[] MISMATCH_COUNT_UNPACKED = {
            "\t\tfor (int i = 0; i <= PARAMETER - 1; i++) begin",
            "\t\t   if (iface.<port_name>_mismatch[i]) begin",
            "\t\t       iface.<port_name>_errors[i]++;",
            "\t\t       iface.test_passed = 0;",
            "\t\t   end",
            "\t\tend",
    };

    public CheckerCodegen() throws IOException {
        super(CHECKER_SV);
    }

    public void setOutputs(HashMap<String, PortDescriptor> outputs) {
        for (int index = 0; index < size(); index++) {
            /* Adds ports checking initialization. */
            if (get(index).contains("iface.<port_name>_errors = 0;")) {
                remove(index);
                definePackingAddPort(false, index, outputs, MISMATCH_INIT, MISMATCH_INIT_UNPACKED);
            }

            /* Adds ports checking. */
            else if (get(index).contains("function void mismatch();"))
                definePackingAddPort(false, index += 3, outputs, MISMATCH, MISMATCH_UNPACKED);

            /* Adds errors counting. */
            else if (get(index).contains("function void countError();"))
                definePackingAddPort(false, ++index, outputs, MISMATCH_COUNT, MISMATCH_COUNT_UNPACKED);
        }
    }
}
