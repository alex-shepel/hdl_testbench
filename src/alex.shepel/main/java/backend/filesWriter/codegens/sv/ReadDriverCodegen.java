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
public class ReadDriverCodegen extends SVCodegen implements BackendParameters {

    /* The template of code for initialization of ReadGenerator object.
    Used when unpacked size of input port equals to 0. */
    private static final String[] GENERATOR_INIT = {
            "\t\tthis.gen_<port_name> = new(iface);",
            "\t\tthis.gen_<port_name>.open({$sformatf(\"%s\", filePath), \"/<port_name>.tbv\"});",
    };

    /* The template of code for initialization of ReadGenerator object.
    Used when unpacked size of input port larger then 0. */
    private static final String[] GENERATOR_INIT_UNPACKED = {
            "\t\tfor (int i = 0; i <= PARAMETER - 1; i++) begin",
            "\t\t    this.gen_<port_name>[i] = new(iface);",
            "\t\t    this.gen_<port_name>[i].open({$sformatf(\"%s\", filePath), \"/<port_name>_\", $sformatf(\"%0d\", i), \".tbv\"});",
            "\t\tend"
    };

    /* The template of code for running of ReadGenerator object.
    Used when unpacked size of input port equals to 0. */
    private static final String[] GENERATOR_RUN = {
            "\t\tiface.<port_name> = gen_<port_name>.getPoint();",
            "\t\tgen_<port_name>.setIndex(gen_<port_name>.getIndex() + 1);"
    };

    /* The template of code for running of ReadGenerator object.
    Used when unpacked size of input port larger then 0. */
    private static final String[] GENERATOR_RUN_UNPACKED = {
            "\t\tfor (int i = 0; i <= PARAMETER - 1; i++) begin",
            "\t\t    iface.<port_name>[i] = gen_<port_name>[i].getPoint();",
            "\t\t    gen_<port_name>[i].setIndex(gen_<port_name>[i].getIndex() + 1);",
            "\t\tend"
    };

    private static final String[] CHECK_SIZE = {
            "\t\tisTrue = isTrue && (this.gen_prev_<port_name>.getSize() == this.gen_next_<port_name>.getSize());",
    };

    private static final String[] CHECK_SIZE_UNPACKED = {
            "\t\tfor (int i = 0; i < PARAMETER - 1; i++) begin",
            "\t\t    isTrue = isTrue && (this.gen_<port_name>[i].getSize() == this.gen_<port_name>[i + 1].getSize());",
            "\t\tend"
    };

    private static final String[] CHECK_ENDING = {
            "\t\treturn gen_<port_name>.getIndex() >= gen_<port_name>.getSize() - 1;",
    };

    private static final String[] CHECK_ENDING_UNPACKED = {
            "\t\treturn gen_<port_name>[0].getIndex() >= gen_<port_name>[0].getSize() - 1;",
    };

    private static final String[] GET_SIZE = {
            "\t\treturn gen_<port_name>.getSize();",
    };

    private static final String[] GET_SIZE_UNPACKED = {
            "\t\treturn gen_<port_name>[0].getSize();",
    };

    /**
     * The class constructor.
     *
     * @throws IOException "ReadDriver.sv" file can't be read
     *                     (file stores in the resource directory).
     */
    public ReadDriverCodegen() throws IOException {
        super(READ_DRIVER_SV);
    }

    /**
     * Overwrites fields in the file
     * where must be placed code
     * for reading input dattors
     * for each of DUT's inputs.
     *
     * @param inputs The HashMap object that contains DUT's inputs.
     *              Key contain a port name.
     *              Value contain PortDescriptor object.
     */
    public void setInputs(HashMap<String, PortDescriptor> inputs) {
        for (int index = 0; index < size(); index++) {
            /* Adds ReadGenerator object declaration. */
            if (get(index).contains("ReadGenerator #(DATA_WIDTH)") && get(index).contains("// inputs")) {
                remove(index);
                addGeneratorsDeclaration(index, inputs);
            }

            /* Fills in ReadGenerator initialization field. */
            else if (get(index).contains("local function void initGens();"))
                definePackingAddPort(false, ++index, inputs, GENERATOR_INIT, GENERATOR_INIT_UNPACKED);

            /* Fills in ReadGenerator running field. */
            else if (get(index).contains("function void run()"))
                definePackingAddPort(false, ++index, inputs, GENERATOR_RUN, GENERATOR_RUN_UNPACKED);

            /* Fills in body of the function that controls correctness of input data. */
            else if (get(index).contains("local function bit checkSize()"))
                addSizeChecking(index += 2, inputs);

            else if (get(index).contains("function bit isEnding()"))
                definePackingAddPort(true, ++index, inputs, CHECK_ENDING, CHECK_ENDING_UNPACKED);

            else if (get(index).contains("function int getSize()"))
                definePackingAddPort(true, ++index, inputs, GET_SIZE, GET_SIZE_UNPACKED);
        }
    }

    /**
     * Overwrites fields in the file
     * where must be placed code
     * for reading input dattors
     * for each of DUT's outputs.
     *
     * @param outputs The HashMap object that contains DUT's outputs.
     *              Key contain a port name.
     *              Value contain PortDescriptor object.
     */
    public void setOutputs(HashMap<String, PortDescriptor> outputs) {
        outputs = addExpectedNames(outputs);

        for (int index = 0; index < size(); index++) {
            /* Adds ReadGenerator object declaration. */
            if (get(index).contains("ReadGenerator #(DATA_WIDTH)") && get(index).contains("// expected outputs")) {
                remove(index);
                addGeneratorsDeclaration(index, outputs);
            }

            /* Fills in ReadGenerator initialization field. */
            else if (get(index).contains("local function void initGens();"))
                definePackingAddPort(false, ++index, outputs, GENERATOR_INIT, GENERATOR_INIT_UNPACKED);

            /* Fills in ReadGenerator running field. */
            else if (get(index).contains("function void run()"))
                definePackingAddPort(false, ++index, outputs, GENERATOR_RUN, GENERATOR_RUN_UNPACKED);
        }
    }

    private HashMap<String, PortDescriptor> addExpectedNames(HashMap<String, PortDescriptor> outputs) {
        final HashMap<String, PortDescriptor> expectedOutputs = new HashMap<>();

        for (final PortDescriptor desc: outputs.values()) {
            final PortDescriptor expectedDesc = desc.deepCopy();
            expectedDesc.setName(desc.getName() + "_expect");
            expectedOutputs.put(expectedDesc.getName(), expectedDesc);
        }

        return expectedOutputs;
    }

    /**
     * Adds a code lines for declaration of ReadGenerator objects.
     *
     * @param index The index of a line in the parsed file
     *              where must be placed current code.
     * @param ports The HashMap object that contains DUT's ports.
     *              Key contain a port name.
     *              Value contain PortDescriptor object.
     */
    private void addGeneratorsDeclaration(int index, HashMap<String, PortDescriptor> ports) {
        for (String name: ports.keySet()) {
            if (!name.toLowerCase().contains("clk") && !name.toLowerCase().contains("clock")) {
                /* When unpacked size of port equals 0. */
                if (ports.get(name).getUnpackedSize().equals("")) {
                    String packedSize = decodeSizeDeclaration(ports.get(name).getPackedSize());
                    add(index,
                        "\tReadGenerator #(" + packedSize + ") gen_" + name + ";");
                }

                /* When unpacked size of port is larger then 0. */
                else {
                    String packedSize = decodeSizeDeclaration(ports.get(name).getPackedSize());
                    String unpackedSize = decodeSizeDeclaration(ports.get(name).getUnpackedSize());
                    add(index,
                        "\tReadGenerator #(" + packedSize + ") gen_" + name + " [" + unpackedSize + "];");
                }

                add(index, "\t// Port: " + ports.get(name).toString());
                add(index, "");
            }
        }
    }

    /**
     * Adds a code lines for description of checkOneSize() function.
     *
     * @param index The index of a line in the parsed file
     *              where must be placed current code.
     * @param inputs The HashMap object that contains DUT's inputs.
     *              Key contain a port name.
     *              Value contain PortDescriptor object.
     */
    private void addSizeChecking(int index, HashMap<String, PortDescriptor> inputs) {
        String previousName = "null";

        for (String name: inputs.keySet()) {
            if (!name.toLowerCase().contains("clk") && !name.toLowerCase().contains("clock")) {
                /* Checking unpacked inputs. */
                if (!inputs.get(name).getUnpackedSize().equals("")) {
                    String unpackedSize = decodeSizeReferencing(inputs.get(name).getUnpackedSize());

                    for (int lineNum = CHECK_SIZE_UNPACKED.length - 1; lineNum >= 0; lineNum--) {
                        String editedLine = CHECK_SIZE_UNPACKED[lineNum].replace("<port_name>", name);
                        editedLine = editedLine.replace("PARAMETER - 1", decodeSizeReferencing(unpackedSize));
                        add(index, editedLine);
                    }

                    add(index, "\t\t// Port: " + inputs.get(name).toString());
                    add(index, "");
                }

                /* Checking packed inputs. */
                if (!previousName.equals("null")) {
                    String editedLine = CHECK_SIZE[0].replace("prev_<port_name>", previousName);
                    if (!inputs.get(previousName).getUnpackedSize().equals("")) {
                        editedLine = editedLine.replace(previousName, previousName + "[0]");
                    }
                    editedLine = editedLine.replace("next_<port_name>", name);
                    if (!inputs.get(name).getUnpackedSize().equals("")) {
                        editedLine = editedLine.replace(name, name + "[0]");
                    }
                    add(index, editedLine);
                    add(index, "\t\t//    " + inputs.get(name).toString());
                    add(index, "\t\t//    " + inputs.get(previousName).toString());
                    add(index, "\t\t// Ports: ");
                    add(index, "");
                }

                previousName = name;
            }
        }
    }
}
