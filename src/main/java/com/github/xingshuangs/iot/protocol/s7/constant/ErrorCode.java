/*
 * MIT License
 *
 * Copyright (c) 2021-2099 Oscura (xingshuang) <xingshuang_cool@163.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.xingshuangs.iot.protocol.s7.constant;


import java.util.HashMap;
import java.util.Map;

/**
 * Error code.
 * Error codes
 *
 * @author xingshuang
 */
public class ErrorCode {

    private ErrorCode() {
        // NOOP
    }

    public static final Map<Integer, String> MAP = new HashMap<>();

    static {
        init();
    }

    private static void init() {
        MAP.put(0x0000, "There are no errors");
        MAP.put(0x0110, "The block number is invalid");
        MAP.put(0x0111, "The request length is invalid");
        MAP.put(0x0112, "The parameter is invalid");
        MAP.put(0x0113, "The block type is invalid");
        MAP.put(0x0114, "Block not found");
        MAP.put(0x0115, "The block already exists");
        MAP.put(0x0116, "Blocks are write-protected");
        MAP.put(0x0117, "The block/OS update is too large");
        MAP.put(0x0118, "The block number is invalid");
        MAP.put(0x0119, "The password entered is incorrect");
        MAP.put(0x011A, "The PG resource is incorrect");
        MAP.put(0x011B, "The PLC resource is incorrect");
        MAP.put(0x011C, "Protocol error");
        MAP.put(0x011D, "Too many blocks (module-related limitations)");
        MAP.put(0x011E, "The connection to the database is no longer established, or the S7DOS handle is invalid");
        MAP.put(0x011F, "As a result, the buffer is too small");
        MAP.put(0x0120, "Block end list");
        MAP.put(0x0140, "The available memory is insufficient");
        MAP.put(0x0141, "The job could not be processed due to lack of resources");
        MAP.put(0x8001, "When the block is in its current state, the requested service cannot be executed");
        MAP.put(0x8003, "S7 Protocol Error: An error occurred while transferring the block");
        MAP.put(0x8100, "Application, General Error: Service where the remote module is unknown");
        MAP.put(0x8104, "This service was not implemented on the module or a frame error was reported");
        MAP.put(0x8204, "The type specifications of the objects are inconsistent");
        MAP.put(0x8205, "The copied block already exists and is not linked");
        MAP.put(0x8301, "There is not enough memory space or working memory on the module, or the specified storage media is inaccessible");
        MAP.put(0x8302, "There are too few available resources or processor resources are unavailable");
        MAP.put(0x8304, "Further parallel uploads are not possible. There is a resource bottleneck");
        MAP.put(0x8305, "Features are not available");
        MAP.put(0x8306, "Low working memory (for copying, linking, loading AWP)");
        MAP.put(0x8307, "Insufficient retaining working memory (for copying, linking, loading AWP)");
        MAP.put(0x8401, "S7 Protocol Error: Invalid service sequence (e.g., loading or uploading blocks)");
        MAP.put(0x8402, "The service cannot be executed due to the state of the addressed object");
        MAP.put(0x8404, "S7 protocol: This function cannot be performed");
        MAP.put(0x8405, "The remote block is in the DISABLE state (CFB). This function cannot be performed");
        MAP.put(0x8500, "S7 Protocol Error: Frame error");
        MAP.put(0x8503, "Alert from module: Premature service cancellation");
        MAP.put(0x8701, "Error addressing object on communication partner (for example, wrong zone length)");
        MAP.put(0x8702, "The requested service is not supported by the module");
        MAP.put(0x8703, "Deny access to the object");
        MAP.put(0x8704, "Access error: Object is corrupted");
        MAP.put(0xD001, "Protocol error: Invalid job number");
        MAP.put(0xD002, "Parameter error: Illegal job variant");
        MAP.put(0xD003, "Parameter error: The module does not support the debugging function");
        MAP.put(0xD004, "Parameter error: The job status is invalid");
        MAP.put(0xD005, "Parameter error: The job is terminated invalidly");
        MAP.put(0xD006, "Parameter Error: Illegal link disconnect ID");
        MAP.put(0xD007, "Parameter error: The number of buffer elements is invalid");
        MAP.put(0xD008, "Parameter error: The scan rate is invalid");
        MAP.put(0xD009, "Parameter error: The number of executions is invalid");
        MAP.put(0xD00A, "Parameter error: The event is triggered illegally");
        MAP.put(0xD00B, "Parameter Error: The trigger condition is invalid");
        MAP.put(0xD011, "Error in the parameter in the call environment path: The block does not exist");
        MAP.put(0xD012, "Parameter Error: The address in the block is incorrect");
        MAP.put(0xD014, "Parameter error: Deleting/overwriting block");
        MAP.put(0xD015, "Parameter error: The tag address is invalid");
        MAP.put(0xD016, "Parameter error: The job could not be tested due to a user program error");
        MAP.put(0xD017, "Parameter error: Invalid trigger number");
        MAP.put(0xD025, "Parameter error: The path is invalid");
        MAP.put(0xD026, "Parameter error: Illegal access type");
        MAP.put(0xD027, "Parameter error: This number of data blocks is not allowed");
        MAP.put(0xD031, "Internal protocol error");
        MAP.put(0xD032, "Parameter error: The result buffer length is incorrect");
        MAP.put(0xD033, "Protocol error: The job length is incorrect");
        MAP.put(0xD03F, "Encoding error: Error in part of the parameter (e.g., reserved bytes are not equal to 0)");
        MAP.put(0xD041, "Data error: Illegal status list ID");
        MAP.put(0xD042, "Data error: The label address is invalid");
        MAP.put(0xD043, "Data error: The referenced job could not be found, check the job data");
        MAP.put(0xD044, "Data error: The tag value is invalid, check the job data");
        MAP.put(0xD045, "Data error: ODIS control is not allowed in HOLD");
        MAP.put(0xD046, "Data error: Illegal measurement phase during runtime measurement");
        MAP.put(0xD047, "Data error: Illegal hierarchy in Read Job List");
        MAP.put(0xD048, "Data error: Illegal deletion ID in 'Delete Job'.");
        MAP.put(0xD049, "The replacement ID in the Replace Job is invalid");
        MAP.put(0xD04A, "An error occurred while executing 'Program Status'");
        MAP.put(0xD05F, "Encoding error: Error in the data section (e.g., reserved bytes are not equal to 0,... ）");
        MAP.put(0xD061, "Resource error: There is no memory space for the job");
        MAP.put(0xD062, "Resource error: The job list is full");
        MAP.put(0xD063, "Resource error: The event is occupied");
        MAP.put(0xD064, "Resource error: Not enough memory for a result buffer element");
        MAP.put(0xD065, "Resource error: Not enough memory for more than one result buffer element");
        MAP.put(0xD066, "Resource error: A timer available for runtime measurement is occupied by another job");
        MAP.put(0xD067, "Resource error: Too many 'Modify Mark' operations (especially multi-processor operations)");
        MAP.put(0xD081, "Function not allowed in current mode");
        MAP.put(0xD082, "Mode error: Unable to exit HOLD mode");
        MAP.put(0xD0A1, "Function not allowed at the current protection level");
        MAP.put(0xD0A2, "Cannot run at this time because the function being run will modify memory");
        MAP.put(0xD0A3, "Too many 'Modify Mark' jobs active on I/O (especially multiprocessor operations)");
        MAP.put(0xD0A4, "'Forced' has been established");
        MAP.put(0xD0A5, "The referenced job was not found");
        MAP.put(0xD0A6, "Unable to disable/enable job");
        MAP.put(0xD0A7, "Cannot delete the job, e.g. because it is currently being read");
        MAP.put(0xD0A8, "Cannot replace the job, for example because it is currently being read or deleted");
        MAP.put(0xD0A9, "Cannot read the job, for example because it is currently being deleted");
        MAP.put(0xD0AA, "Processing operation exceeded time limit");
        MAP.put(0xD0AB, "Invalid job parameters in process operation");
        MAP.put(0xD0AC, "Invalid job data in process operation");
        MAP.put(0xD0AD, "Operation mode has been set");
        MAP.put(0xD0AE, "The job was set up via a different connection and can only be processed via this connection");
        MAP.put(0xD0C1, "At least one error was detected while accessing a tag");
        MAP.put(0xD0C2, "Switch to STOP / HOLD mode");
        MAP.put(0xD0C3, "At least one error was detected while accessing a tag. Mode changed to STOP / HOLD");
        MAP.put(0xD0C4, "Timeout during runtime measurement");
        MAP.put(0xD0C5, "Display of block stack is inconsistent because blocks were deleted/reloaded");
        MAP.put(0xD0C6, "The job has been deleted because the job it referenced has been deleted");
        MAP.put(0xD0C7, "The job was automatically deleted because the STOP mode was exited");
        MAP.put(0xD0C8, "Block status aborted due to inconsistency between test job and running program");
        MAP.put(0xD0C9, "Exit the status area by resetting OB90");
        MAP.put(0xD0CA, "Exit status range by resetting OB90 and accessing the error read tag before exiting");
        MAP.put(0xD0CB, "Output disable of peripheral output is activated again");
        MAP.put(0xD0CC, "The amount of data for debugging function is limited by time");
        MAP.put(0xD201, "Syntax error in block name");
        MAP.put(0xD202, "Syntax error in function parameters");
        MAP.put(0xD205, "Link block already exists in RAM: conditional copy is not possible");
        MAP.put(0xD206, "Link block already exists in EPROM: conditional copy is not possible");
        MAP.put(0xD208, "Exceeded the maximum number of copied (unlinked) blocks of the module");
        MAP.put(0xD209, "(At least) one of the given blocks was not found on the module");
        MAP.put(0xD20A, "Exceeded the maximum number of blocks that can be linked to a job");
        MAP.put(0xD20B, "Exceeded the maximum number of blocks that can be deleted by a job");
        MAP.put(0xD20C, "OB cannot be copied because the associated priority does not exist");
        MAP.put(0xD20D, "SDB cannot be interpreted (e.g., unknown number)");
        MAP.put(0xD20E, "No (further) blocks available");
        MAP.put(0xD20F, "Exceeded module-specific maximum block size");
        MAP.put(0xD210, "Invalid block number");
        MAP.put(0xD212, "Incorrect header attributes (runtime related)");
        MAP.put(0xD213, "Too many SDBs. Please note the restrictions on the modules being used");
        MAP.put(0xD216, "Invalid user program - Reset module");
        MAP.put(0xD217, "The protection level specified in the module properties is not allowed");
        MAP.put(0xD218, "Incorrect attributes (active/passive)");
        MAP.put(0xD219, "The block length is incorrect (for example, the length of the first part or the entire block is incorrect)");
        MAP.put(0xD21A, "Incorrect local data length or write protection error");
        MAP.put(0xD21B, "Module cannot compress or compress early interrupt");
        MAP.put(0xD21D, "The amount of dynamic item data transferred is illegal");
        MAP.put(0xD21E, "Cannot assign parameters to module (e.g. FM, CP). System data cannot be linked");
        MAP.put(0xD220, "Invalid programming language. Please note the restrictions on the modules being used");
        MAP.put(0xD221, "Invalid system data for connection or routing");
        MAP.put(0xD222, "The system data defined by global data contains invalid parameters");
        MAP.put(0xD223, "The instance data block of the communication function block is wrong or exceeds the maximum number of background data blocks");
        MAP.put(0xD224, "SCAN system data block contains invalid parameters");
        MAP.put(0xD225, "DP system data block contains invalid parameters");
        MAP.put(0xD226, "Structural error in block");
        MAP.put(0xD230, "Structural error in block");
        MAP.put(0xD231, "At least one loaded OB could not be copied because the associated priority does not exist");
        MAP.put(0xD232, "At least one block number of the loaded block is illegal");
        MAP.put(0xD234, "The block exists twice in the specified memory medium or job");
        MAP.put(0xD235, "This block contains an incorrect checksum");
        MAP.put(0xD236, "This block does not contain a checksum");
        MAP.put(0xD237, "You are about to load the block twice, i.e. a block with the same timestamp already exists on the CPU");
        MAP.put(0xD238, "At least one of the specified blocks is not a DB");
        MAP.put(0xD239, "At least one of the specified DBs is not available as a link variable in the load memory");
        MAP.put(0xD23A, "At least one of the specified DBs is significantly different from the copied and linked variants");
        MAP.put(0xD240, "Coordination rule violated");
        MAP.put(0xD241, "The current protection level does not allow this function");
        MAP.put(0xD242, "Protection conflict when processing F block");
        MAP.put(0xD250, "Update and module ID or version do not match");
        MAP.put(0xD251, "Incorrect operating system component sequence");
        MAP.put(0xD252, "Checksum error");
        MAP.put(0xD253, "No executable loader available; can only be updated using a memory card");
        MAP.put(0xD254, "Storage error in operating system");
        MAP.put(0xD280, "Error when compiling block in S7-300 CPU");
        MAP.put(0xD2A1, "Another block function or trigger on the block is active");
        MAP.put(0xD2A2, "The trigger on the block is active. First complete the debug function");
        MAP.put(0xD2A3, "The block is not activated (linked), the block is occupied or the block is currently marked for deletion");
        MAP.put(0xD2A4, "This block has been processed by another block function");
        MAP.put(0xD2A6, "Cannot save and modify user program at the same time");
        MAP.put(0xD2A7, "The block has the 'unlinked' attribute or is not processed");
        MAP.put(0xD2A8, "Activated debug features prevent parameters from being assigned to the CPU");
        MAP.put(0xD2A9, "Allocating new parameters to CPU");
        MAP.put(0xD2AA, "Currently assigning new parameters to the module");
        MAP.put(0xD2AB, "Currently changing dynamic configuration limits");
        MAP.put(0xD2AC, "The running activation or deactivation assignment (SFC 12) temporarily blocks the R-KiR process");
        MAP.put(0xD2B0, "Error occurred while configuring in RUN (CiR)");
        MAP.put(0xD2C0, "The maximum number of technology objects has been exceeded");
        MAP.put(0xD2C1, "The same technology data block already exists on the module");
        MAP.put(0xD2C2, "Unable to download user program or download hardware configuration");
        MAP.put(0xD401, "Information function unavailable");
        MAP.put(0xD402, "Information function unavailable");
        MAP.put(0xD403, "Service logged in/out (diagnostics/PMC)");
        MAP.put(0xD404, "Maximum number of nodes reached. No longer need to log in to diagnostics/PMC");
        MAP.put(0xD405, "Unsupported service or syntax error in function parameters");
        MAP.put(0xD406, "Required information currently not available");
        MAP.put(0xD407, "A diagnostic error occurred");
        MAP.put(0xD408, "Update aborted");
        MAP.put(0xD409, "DP bus error");
        MAP.put(0xD601, "Syntax error in function parameters");
        MAP.put(0xD602, "The password entered is incorrect");
        MAP.put(0xD603, "The connection has been legalized");
        MAP.put(0xD604, "Connection enabled");
        MAP.put(0xD605, "Cannot be legalized because password does not exist");
        MAP.put(0xD801, "At least one tag address is invalid");
        MAP.put(0xD802, "The specified job does not exist");
        MAP.put(0xD803, "illegal working status");
        MAP.put(0xD804, "Illegal cycle time (illegal time base or multiple)");
        MAP.put(0xD805, "Cannot set up cyclic read job anymore");
        MAP.put(0xD806, "The referenced job is in a state where it cannot perform the requested function");
        MAP.put(0xD807, "Function aborted due to overload, which means that the time required to execute the read cycle is longer than the set scan cycle time");
        MAP.put(0xDC01, "Invalid date and/or time");
        MAP.put(0xE201, "CPU is already the master device");
        MAP.put(0xE202, "Cannot connect and update due to different user programs in the flash module");
        MAP.put(0xE203, "Cannot connect and update due to different firmware");
        MAP.put(0xE204, "Cannot connect and update due to different memory configurations");
        MAP.put(0xE205, "Connect/update aborted due to synchronization error");
        MAP.put(0xE206, "Connection/update refused due to coordination violation");
        MAP.put(0xEF01, "S7 protocol error: ID2 error; only 00H is allowed during operation");
        MAP.put(0xEF02, "S7 protocol error: ID2 error; resource set does not exist");
    }
}
