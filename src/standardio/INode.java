
package standardio;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author Aaron Foltz
 * 
 */
public class INode implements Serializable {

	int					iNodeBlockNumber;
	String				fileName, permission;
	ArrayList<Integer>	dataBlockNumber	= new ArrayList<Integer>();

	public INode(String fileName, int block, String permission) {

		this.fileName = fileName;
		iNodeBlockNumber = block;
		this.permission = permission;
	}

	public boolean addDataBlockNumber(int blockNumber) {

		dataBlockNumber.add(blockNumber);
		return true;
	}

	public ArrayList getDataBlockNumber() {

		return dataBlockNumber;
	}

	public String getFileName() {

		return fileName;
	}

	public int getINodeBlockNumber() {

		return iNodeBlockNumber;
	}

	public String getPermission() {

		return permission;
	}

}
