package com.elende.MirthSNMP;

import java.io.IOException;
import java.util.List;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.PDUFactory;
import org.snmp4j.util.TableEvent;
import org.snmp4j.util.TableUtils;


    
public class SNMPManager {

	
    Snmp snmp = null;  // this is the SNMP session
    String address = null;		//This is the IP address of the box we're querying
    String community ="public";
    int retries = 2;
    long timeout=1500;

    public SNMPManager(String add)
    {
    	address = add;
     }

    
    public SNMPManager(String add,String comty, int rts, long tmo)
    {
    	address = add;
    	community = comty;
    	retries =rts;
    	timeout = tmo;
    	
    }

    
    
    
    
    /**
    * Start the Snmp session. If you forget the listen() method you will not
    * get any answers because the communication is asynchronous
    * and the listen() method listens for answers.
    * @throws IOException
    */
    
    private void start() throws IOException {
    		TransportMapping transport = new DefaultUdpTransportMapping();
    		snmp = new Snmp(transport);
    		// Do not forget this line!
    		transport.listen();
    }

    /**
    * Method which takes a single OID and returns the response from the agent as a String.
    * @param oid
    * @return
    * @throws IOException
    */
    public String getAsString(OID oid) throws IOException {
    	ResponseEvent event = getValue(new OID[] { oid });
    	return event.getResponse().get(0).getVariable().toString();
    }

    /**
    * This method is capable of handling multiple OIDs
    * @param oids
    * @return
    * @throws IOException
    */
    public ResponseEvent getValue(OID oids[]) throws IOException {
    PDU pdu = new PDU();
    
    	for (OID oid : oids) {
    			pdu.add(new VariableBinding(oid));
    	}
    	
    	pdu.setType(PDU.GET);
    	
    	ResponseEvent event = snmp.send(pdu, getTarget(), null);
    	if(event != null) {
    			return event;
    	}
       throw new RuntimeException("GET timed out");
    }
    
    
    
    
    /**
     * Synchronously returns a list of TableEvents ... useful for retrieving a table..
     * @return 
     * 
     * @throws IOException
     */
    public List<TableEvent>  getifTable(OID[] columns) throws IOException
    {
      List<TableEvent> evs = null;
      TableUtils tutils = new TableUtils(snmp, new DefaultPDUFactory());
    	  evs =  tutils.getTable(getTarget(), columns, null,null);
         
      return evs;
    }

    
    
    /**
    * This method returns a Target, which contains information about
    * where the data should be fetched and how.
    * @return
    */
    private Target getTarget() {
    	
    	Address targetAddress = GenericAddress.parse(address);
    	CommunityTarget target = new CommunityTarget();
    	target.setCommunity(new OctetString(community));
    	target.setAddress(targetAddress);
    	target.setRetries(retries);
    	target.setTimeout(timeout);
    	target.setVersion(SnmpConstants.version2c);
    	
      return target;
    }

    
    

public static void main(String[] args) throws IOException {
/**
* Port 161 is used for Read and Other operations
* Port 162 is used for the trap generation
*/
	SNMPManager client = new SNMPManager("udp:192.168.1.36/161");
	client.start();
/**
* OID - .1.3.6.1.2.1.1.1.0 => SysDec
* OID - .1.3.6.1.2.1.1.5.0 => SysName
* => MIB explorer will be usefull here, as discussed in previous article
*/
	String sysDescr = client.getAsString(new OID(".1.3.6.1.2.1.1.1.0"));
	System.out.println(sysDescr);

	//client.getifTable();

	
	
	
	
	
	
/* Hook on to ifTable */

	}
}

    
 /*
 
    Useful Mirth OID's
    
     .1.3.6.1.4.1.29009.2.1.1 - Appliance Model
     .1.3.6.1.4.1.29009.2.1.2 - Appliance Desc
     .1.3.6.1.4.1.29009.2.1.3 - Appliance Name
     .1.3.6.1.4.1.29009.2.1.4 - Appliance UpTime
    
    
    */