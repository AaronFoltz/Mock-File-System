
package standardio;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Aaron Foltz
 * 
 */
public class StandardIO {

	private final Scanner			scanner			= new Scanner(System.in);
	// menuScanner = generic scanner for file names/modes from the CLI
	private final Scanner			menuScanner		= new Scanner(System.in);
	// writeScanner reads entire line of what the user wishes to write
	private final Scanner			writeScanner	= new Scanner(System.in);
	// inputScanner = Scanner for file names/modes
	private final Scanner			inputScanner	= new Scanner(System.in);

	// The following objects are needed to correctly serialize/deserialize
	// the necessary objects. There are separate
	private ObjectOutput			out;
	private ObjectInput				inNode, inStruct, in;
	private ByteArrayInputStream	bais;
	private ByteArrayOutputStream	baos;

	// Creates the new simulated disk
	private final Disk				disk			= new Disk();

	// Open File list - there should only be 10 files open at a time.
	private final ArrayList<INode>	openFiles		= new ArrayList<INode>(10);

	// List containing the free INode blocks on the disk
	private final TreeSet<Integer>	freeINodeList	= new TreeSet<Integer>();

	// List of EVERY file used - the directory structure
	private final LinkedList<INode>	files			= new LinkedList<INode>();

	// Array to hold the indices of block numbers for a given file
	ArrayList						dataBlockArray;

	// exit - Determines if the user wants to exit the program
	// hit - determines if a file is in the directory
	private boolean					exit, hit = false;

	// fd = file descriptor, an index to the table of open files
	// input = the menu item selected by the user
	// x = an iterator for printing out the files
	// freeSpace = integer used when choosing an empty block for new files
	// freeINodeSpace = integer used to choose empty block for the INode and
	// filestruct
	private int						fd, x, freeDataSpace, freeINodeSpace;

	// Block Number used during seek
	// Last file is the last block used on the disk
	private int						block_number;
	private final int				lastFile		= 0;

	// List containing the free data blocks on the disk
	private final int[]				freeList		= new int[1000];

	// captured = the strings captured upon deserialization
	// iNode has the block number and permissions
	// fileStruct contains the iNode as well as the file's name
	// name = desired filename entered by the user
	// permission = desired permission entered from the user
	// input2 - gathered from the user for menu purposes
	private String					captured, iNode, fileStruct, name,
			permission;
	private String					input2			= "";
	private String					input;
	private int						numberInput;

	// Split is used to split the iNodes and fileStructs in order to gather
	// information
	private String[]				split;

	// Byte array to be read from the disk
	private final char[]			readBuffer		= new char[1000];

	// Byte array to be written to the disk
	private final byte[]			writeBuffer		= new byte[1000];
	// Temp buffer in order to make the write buffer the correct size
	private byte[]					tempBuffer;

	// Buffer is used to gather the serialized data for the iNode
	private byte[]					buffer			= new byte[1000];
	// buffer2 is used to gather the serialized data for the fileStruct
	private final byte[]			buffer2			= new byte[1000];

	public StandardIO() {

		// This regenerates the file system from the last time
		initializeINodes();
		// this.initializeFileStructs();

		// Loop through the menu until the user exits
		while (!exit) {

			printMenu();

			// Gathers the input from the user
			input = scanner.nextLine();
			try {
				// Parses the int from what the user entered
				numberInput = Integer.parseInt(input);
				// If the entered input is not a number, then skip through to
				// the next iteration
			} catch (NumberFormatException nfe) {
				numberInput = -1;
				System.out.println("\nThat is not an option in this menu!");
			}

			// Re-Initialize System to a clean slate
			if (numberInput == 1) {
				f_initialize();

				// View Files Listing in File System
			} else if (numberInput == 2) {
				x = 0;
				System.out.println("\nFile Number\tName\t\tPermissions\t" +
						"Location(s) on disk");
				// Iterates through all open files
				for (INode file : files) {

					// Appropriately formats the output
					System.out.printf("%-18d %-20s%-3s\t\t%s\n", x++, file
							.getFileName(),
							file.getPermission(), file.getDataBlockNumber());
				}

				// View File Descriptors - this should just be output of
				// the file descriptor (indexes of open table) with its
				// corresponding
				// file name in that table
			} else if (numberInput == 3) {
				x = 0;
				System.out.println("\nFile Number\tName\t\tPermissions\t" +
						"Location on disk");

				// Iterate through every file - this is needed to get the file
				// name
				for (INode file : openFiles) {

					// Appropriately formats the output
					System.out.printf("%-18d %-20s%-3s\t\t%s\n", x++,
							file.getFileName(), file.getPermission(), file
									.getDataBlockNumber());

				}

				// Create and Open a File
			} else if (numberInput == 4) {
				System.out.print("Please enter a file name: ");
				name = scanner.nextLine();
				if (f_open(name.toCharArray()) == -1) {
					// Inform user that there are too many files open
					System.out.println("File open limit reached! (10)");
				}

				// Write in File
			} else if (numberInput == 5) {
				System.out.print("Please enter a file name: ");
				name = scanner.nextLine();

				// Traverses the files that are open to see if the needed file
				// exists
				for (INode openFile : files) {

					// If the file name is present
					if (openFile.getFileName().equals(name)) {
						// The file is at least in the directory
						hit = true;

						// Get the file descriptor of the file (index into open
						// file table)
						fd = openFiles.indexOf(openFile);

						// Error checking code - if the file is not open yet
						if (fd == -1) {
							System.out
									.println("\nThe File is not open yet, please open it if you wish to work with it");
						}
						// The file is open, so we may proceed with writing to
						// the file if it has the correct permissions
						else if ((fd != -1)
								&& (openFile.getPermission().equals("w") || openFile
										.getPermission().equals("rw"))) {

							System.out.print("What would you like to write?: ");

							// Error checking code from the f_write function
							if (f_write(fd, scanner.nextLine().toCharArray()) == -1) {
								System.out
										.println("\nThere was a problem writing to the file");
							}

							// Error Checking code - if the file does not have
							// write permissions
						} else {
							System.out
									.println("\nThe File does not have write privileges");
						}
					}

				}
				// If we didn't find a file, then it doesn't exist
				if (!hit) {
					System.out.println("\nThe file does not exist!");
				}
				hit = false;

				// Read from File
			} else if (numberInput == 6) {
				System.out.print("Please enter a file name: ");
				name = scanner.nextLine();

				// Traverses the files that are available to see if the file
				// exists
				for (INode openFile : files) {

					// If the file name is present
					if (openFile.getFileName().equals(name)) {
						// The file is at least in the directory
						hit = true;

						// Get the file descriptor of the file (index into open
						// file table)
						fd = openFiles.indexOf(openFile);

						// Error checking code - if the file is not open yet
						if (fd == -1) {
							System.out
									.println("\nThe File is not open yet, please open it if you wish to work with it");
						}
						// The file is open, so we may proceed with writing to
						// the file if it has the correct permissions
						else if ((fd != -1)
								&& (openFile.getPermission().equals("r") || openFile
										.getPermission().equals("rw"))) {

							// Error checking code from the f_read function
							if (f_read(fd, readBuffer) == -1) {
								System.out
										.println("\nThere was a problem writing to the file");
							}
							// Error Checking code - if the file does not have
							// read permissions
						} else {
							System.out
									.println("\nThe File does not have read privileges");
						}
					}
				}
				// If we didn't find a file, then it doesn't exist
				if (!hit) {
					System.out.println("\nThe file does not exist!");
				}
				hit = false;

				// Move File Pointer
			} else if (numberInput == 7) {
				System.out.print("Please enter a file name: ");
				name = inputScanner.next();

				// Retrieve the file from the file directory
				for (INode file : files) {
					// Split the file structure to get the file name - to check
					// for a match

					// If the file name is present
					if (split[0].equals(name)) {
						// The file is at least in the directory
						hit = true;

						// Get the file descriptor of the file (index into open
						// file table)
						fd = openFiles.indexOf(split[1] + "," + split[2]);

						// Error checking code - If seek does not return 0,
						// then the file was not open

					}
				}
				// If we didn't find a file, then it doesn't exist
				if (!hit) {
					System.out.println("\nThe file does not exist!");
				} else {
					hit = false;
				}

				// Close File
			} else if (numberInput == 8) {
				System.out.print("Please enter a file name: ");
				name = scanner.nextLine();

				// Retrive the file descriptor from the list of files in the
				// file system
				for (INode openFile : files) {

					// If the file name is present
					if (openFile.getFileName().equals(name)) {
						// The file is at least in the directory
						hit = true;

						// Get the file descriptor of the file (index into open
						// file table)
						fd = openFiles.indexOf(openFile);

						// Error checking code - if close returns -1 then
						// the file was not open
						if (f_close(fd) != 0) {
							System.out.println("\nThe file is not open!");
						}
					}
				}
				// If we didn't find a file, then it doesn't exist
				if (!hit) {
					System.out.println("\nThe file does not exist!");
				}
				hit = false;

				// Delete File
			} else if (numberInput == 9) {
				System.out.print("Please enter a file name: ");
				name = scanner.nextLine();

				// Error checking code - if delete returns -1 then the file
				// was not even on the disk to begin with
				if (f_delete(name.toCharArray()) == -1) {
					System.out.println("\nThe file is not on the disk");
				} else {
					System.out
							.println("\nThe file has been removed from the system");
				}

				// Exit
			} else if (numberInput == 0) {
				System.exit(0);
			}

		}
	}

	/*
	 * f_close will do the necessary operations to close the file from the
	 * system. This means that the file will no longer be in the open file
	 * table, but it will still be in the overall directory of files in the
	 * filesystem
	 */
	private int f_close(int fd) {

		// The File is not open, return error
		if (fd == -1) {
			return -1;
		}

		// Remove the file from the open file table - it will still be in the
		// overall file directory
		openFiles.remove(fd);
		System.out.println("\nThe file has been closed");
		return 0;
	}

	/*
	 * f_delete will take the file out of the open file table (close it), and
	 * will delete it completely from the system. In order to do this, we must
	 * make sure that the data that was written to the file is deleted
	 */
	private int f_delete(char[] filename) {

		// Finds the file in the file list
		for (INode file : files) {
			if (file.getFileName().equals(new String(filename))) {
				try {
					// Close the file first if it was open somehow - No error
					// checking
					// is done because the only error returned is if it is not
					// already open, and in this case we don't care about that
					fd = openFiles.indexOf(file);

					// If the file is open, close it first
					if (fd != -1) {
						f_close(fd);
					}

					// writes 1000 bytes of nothing to the block on the disk
					for (int i = 0; i < 1000; i++) {
						writeBuffer[i] = 0x00;
					}

					// Gathers the locations of the file data
					dataBlockArray = file.getDataBlockNumber();

					// Loops through the data locations of the file and erases
					// them
					for (Object number : dataBlockArray) {
						System.out.println("INTEGER: " + number);
						// Deletes the data on the disk (it starts at block
						// 1000)
						disk.put_block(Integer.parseInt(number.toString()), writeBuffer);
					}

					// Deletes the INode on the disk (it starts at block 0)
					disk.put_block(files.get(fd).getINodeBlockNumber(), writeBuffer);

					// Removes the file from the entire directory
					files.remove(file);

					// Sets the block as free
					freeList[Integer.parseInt(split[1]) - 105] = 1;
					// Sets the block for INodes and file structs as free
					freeINodeList.add((Integer.parseInt(split[1]) - 105));

					return 0;
				} catch (DiskException ex) {
					Logger.getLogger(StandardIO.class.getName())
							.log(Level.SEVERE, null, ex);
				}
			}
		}

		// The file is not on the disk
		return -1;
	}

	/*
	 * f_initialize takes out filesystem back to a clean slate. All iNodes and
	 * fileStructs are deleted as well as the simulated disk
	 */
	private int f_initialize() {

		// This is really block 105 to 155
		for (int i = 0; i < 50; i++) {
			freeList[i] = 1;
		}
		// Clears the free INode list - 0-50
		freeINodeList.clear();
		for (int i = 0; i < 50; i++) {
			freeINodeList.add(i);
		}
		if (openFiles != null) {
			openFiles.clear(); // Clears open file table
		}
		if (files != null) {
			files.clear(); // Clears all files on the disk
		}

		// Creates the buffer which will clear out the disk
		buffer = new byte[64];
		for (int i : buffer) {
			buffer[i] = 0x00;
		}

		// Clears every block on the disk
		for (int i = 0; i < 512; i++) {
			try {
				disk.put_block(i, buffer);
			} catch (DiskException ex) {
				Logger.getLogger(StandardIO.class.getName())
						.log(Level.SEVERE, null, ex);
			}
		}
		return 0;
	}

	/***********************************************************
	 * * * * We need to implement every method that follows. * * * *
	 ***********************************************************/

	/*
	 * When a file is opened, your system should return a file descriptor to the
	 * caller. The file descriptor will be used as a parameter to any subsequent
	 * file system call affecting that open file.
	 * 
	 * This method should be able to support up to 10 open files at a time
	 */
	private int f_open(char[] filename) {

		// If there are >= 10 files open, then inform the user
		if (openFiles.size() >= 10) {
			System.out.println("Max number of files open");
			return -1;
		}

		// Open the file specified by filename if it exists
		for (INode file : files) {

			// If the file name has already been created
			if (file.getFileName().equals(new String(filename))) {

				// Add this already opened file to the list
				// We can guarantee that there is space in the list or it would
				// not have made it here to begin with.
				openFiles.add(file);

				// Returns the file descriptor for the opened file (index into
				// the open file table.
				return openFiles.indexOf(file);
			}
		}

		// Gather wanted permission from the user
		permission = getPermission(permission);

		/*
		 * // Grabs the next block that is free freeINodeList.pollFirst();
		 * if((freeSpace = this.getFreeSpace())==-1){
		 * System.out.println("You cannot have any more files in this filesystem"
		 * ); return -1; }
		 * 
		 * // Changes the bitmap free list to mark the block as "used"
		 * freeList[freeSpace]=1;
		 * 
		 * // Data resides starting at block 1000 freeSpace+=1000;
		 */

		// START SERIALIZATION for INode here

		INode node = new INode(name, freeINodeList.first(), permission);
		// Serialize to a byte array
		try {
			baos = new ByteArrayOutputStream();
			out = new ObjectOutputStream(baos);
			out.writeObject(node);
			out.close();
			baos.close();
		} catch (IOException ex) {
			Logger.getLogger(StandardIO.class.getName())
					.log(Level.SEVERE, null, ex);
		}

		// Get the bytes of the serialized object
		buffer = baos.toByteArray();

		// Make sure the serialized array is length 64
		System.arraycopy(buffer, 0, writeBuffer, 0, buffer.length);
		for (int i = buffer.length + 1; i < 1000; i++) {
			writeBuffer[i] = 0x00;
		}

		// Get a free block for the iNode
		// freeINodeSpace = this.freeINodeList.pollFirst();

		try {
			// Write the needed INode stuff to the disk - at the first free
			// position
			// of the INode list
			disk.put_block(freeINodeList.pollFirst(), writeBuffer);

		} catch (DiskException ex) {
			Logger.getLogger(StandardIO.class.getName())
					.log(Level.SEVERE, null, ex);
		}

		// Adds to the structure where all files are contained
		// FileStruct is a class which contains the filename and the INode
		// The index into this structure can be regarded as the INode number
		files.add(node);

		// Adds the newly opened file to the open file list
		openFiles.add(node);

		// Return the file descriptor for the opened/created file (The file
		// descriptor is really an index into the open file table
		return openFiles.indexOf(node);
	}

	/*
	 * This method will read the contents pointed to by the iNode found in the
	 * open file table. It will be returned to the buffer and then printed out
	 * to standard output
	 */
	private int f_read(int fd, char[] buffer) {

		// Convert it to byte array
		byte[] readOutput = new String(buffer).getBytes();

		dataBlockArray = openFiles.get(fd).getDataBlockNumber();
		try {
			// Read the block from the disk
			for (Object number : dataBlockArray) {
				disk.get_block(Integer.parseInt(number.toString()), readOutput);
				// Prints the file to standard out
				System.out.println("\n" + new String(readOutput));
			}
		} catch (DiskException ex) {
			// This exception checks to see if the buffer to be written is equal
			// to an exact 64 bits.
			Logger.getLogger(StandardIO.class.getName())
					.log(Level.SEVERE, null, ex);
			return -1;
		}

		// Return the current block incremented by 1 - it serves no purpose on
		// this system, but we follow it in order to stick to the specification
		return fd + 1;
	}

	/*
	 * f_write will write the contents of the buffer to the block pointed to by
	 * the iNode at position fd of the open file table. Before writing, we must
	 * make sure that the buffer is stretched out to be of length 64, since we
	 * are only allowed to move 64 byte blocks
	 */
	private int f_write(int fd, char[] buffer) {

		if ((freeDataSpace = getFreeBlockSpace()) == -1) {
			System.out
					.println("You cannot write anymore data in this filesystem");
			return -1;
		} else {
			openFiles.get(fd).addDataBlockNumber(freeDataSpace + 1000);
		}

		// Converts the inputted string to bytes and ensures the array is of
		// length 1000
		tempBuffer = new String(buffer).getBytes();

		// Copies to bigger array depending on the length of the input
		if (tempBuffer.length < 1000) {
			System.arraycopy(tempBuffer, 0, writeBuffer, 0, tempBuffer.length);
		} else {
			System.arraycopy(tempBuffer, 0, writeBuffer, 0, 1000);
		}

		// For loop to "fill in" the remainder of the array with 0 bits
		for (int i = tempBuffer.length; i < 1000; i++) {
			writeBuffer[i] = 0x00;
		}
		try {
			// Add the block to the disk
			disk.put_block(freeDataSpace + 1000, writeBuffer);
		} catch (DiskException ex) {
			// This exception checks to see if the buffer to be written is equal
			// to an exact 64 bits.
			Logger.getLogger(StandardIO.class.getName())
					.log(Level.SEVERE, null, ex);
			return -1;
		}

		// Return the current block imcremented by 1 - it serves no purpose on
		// this system, but is implemented to closely follow the specification
		return fd + 1;
	}

	/*
	 * getBlockNumber does validation checking to gather a block_number from the
	 * user and guarantee that it is of integer input
	 */
	private int getBlockNumber() {

		// input needed to get into the
		int test = -1;

		// Gathers the input from the user
		while (test == -1) {
			try {
				input2 = inputScanner.next();
				// Parses the int from what the user entered
				block_number = Integer.parseInt(input2);

				// Change input, we have the needed input
				test = 0;
				// If the entered input is not a number, then skip through to
				// the next iteration
			} catch (NumberFormatException nfe) {
				test = -1;
				System.out.print("\nPlease enter a number only: ");
			}
		}
		return block_number;
	}

	/*
	 * This method retrieves the FIRST free block on the disk
	 */
	private int getFreeBlockSpace() {

		// If the position in the freeList is 0, then it has not been used yet
		for (int i = 0; i < 1000; i++) {
			if (freeList[i] == 0) {
				return i;
			}
		}
		return -1;

	}

	private String getPermission(String permission) {

		System.out.print("\nPlease enter file mode (r, w, rw): ");
		permission = scanner.nextLine();

		// While user is entering invalid permissions
		while (!testPermission(permission)) {
			System.out
					.print("\nPlease enter file permission again (r, w, rw): ");
			permission = scanner.nextLine();
		}
		return permission;
	}

	private void initializeFileStructs() {

		// The blocks 50-100 will be reserved to hold fileStruct information
		// This includes the file names and a the INode information
		for (int i = 50; i < 100; i++) {
			try {
				// Gather those blocks from the disk
				disk.get_block(i, buffer);

				// If the block actually contains information, then deserialize
				// it
				if (new String(buffer).contains("r")
						|| new String(buffer).contains("w")
						|| new String(buffer).contains("rw")) {
					inStruct = new ObjectInputStream(new ByteArrayInputStream(
							buffer));

					// Get the new FileStruct from the saved fileStruct on file
					captured = (String) inStruct.readObject();
					// Add the regenerated file to the directory/file list
					// files.add(captured);
				}

			} catch (DiskException ex) {
				Logger.getLogger(StandardIO.class.getName())
						.log(Level.SEVERE, null, ex);
			} catch (IOException ex) {
				Logger.getLogger(StandardIO.class.getName())
						.log(Level.SEVERE, null, ex);
			} catch (ClassNotFoundException ex) {
				Logger.getLogger(StandardIO.class.getName())
						.log(Level.SEVERE, null, ex);
			}
		}

	}

	/*
	 * initializeINodes and initializeFileStructs serve as the regeneration code
	 * for our file system. At startup, these methods will traverse through the
	 * iNodes(block 0-49 on the disk) and the fileStructs(block 50-99 on the
	 * disk) in order to fully regenerate the file system.
	 */
	private void initializeINodes() {

		// Set all INode blocks and data blocks to be free
		for (int i = 0; i < 1000; i++) {
			freeINodeList.add(i);
			freeList[i] = 0;
		}

		// The blocks 0-999 are reserved to hold INodes - which include a
		// pointer
		// to the file on the disk
		// This means that our File System has an upper limit of 1000 files
		/*
		 * for(int i = 0;i < 50; i++){ try { // Get the block from the buffer
		 * disk.get_block(i, this.buffer);
		 * 
		 * // Check to see that this block actually contains an INode if(new
		 * String(this.buffer).contains("r")||new
		 * String(this.buffer).contains("w")||new
		 * String(this.buffer).contains("rw")){
		 * 
		 * // Grab the contents to deserialize bais = new
		 * ByteArrayInputStream(this.buffer); inNode = new
		 * ObjectInputStream(bais); captured = (String)inNode.readObject();
		 * 
		 * // If something is contained at that block, then we need to remove //
		 * it from the freeList and freeINodeList split = captured.split(",");
		 * freeList[(Integer.parseInt(split[0])-105)] = 0;
		 * freeINodeList.remove((Integer.parseInt(split[0])-105)); }
		 * 
		 * } catch (DiskException ex) {
		 * Logger.getLogger(StandardIO.class.getName()).log(Level.SEVERE, null,
		 * ex); } catch (IOException ex) {
		 * Logger.getLogger(StandardIO.class.getName()).log(Level.SEVERE, null,
		 * ex); } catch (ClassNotFoundException ex) {
		 * Logger.getLogger(StandardIO.class.getName()).log(Level.SEVERE, null,
		 * ex); } }
		 */
	}

	/*
	 * This method simply prints out the CLI menu to the user
	 */
	private void printMenu() {

		System.out.println("\nUNIX File System Manager");
		System.out.println("1: Re-Initialize System");
		System.out.println("2: View Files Listing in File System (All Files)");
		System.out.println("3: View File Descriptors (Open files)");
		System.out.println("4: Create and Open a File (no spaces)");
		System.out.println("5: Write in File");
		System.out.println("6: Read from File");
		System.out.println("7: Move File Pointer");
		System.out.println("8: Close File");
		System.out.println("9: Delete File");
		System.out.println("0: Exit");
		System.out.print("Enter your choice: ");
		return;
	}

	/*
	 * This method tests to see that the permission entered from the user is a
	 * valid one
	 */
	private boolean testPermission(String permission) {

		// Tests to see that the permissio entered is a valid permission
		if (permission.equalsIgnoreCase("r")
				|| permission.equalsIgnoreCase("w")
				|| permission.equalsIgnoreCase("rw")) {
			return true;
		} else {
			return false;
		}
	}
}
