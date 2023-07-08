package com.example.demo.menager;


import com.example.demo.functions.*;
import com.example.demo.gui.Form;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

//@Component
public class SnmpMenager {
    private String trapAddress = "10.0.1.1/1620";
    private String receivedAgentAddress = null;
    private String receivedAgentId= null;
    private String ip= null;
    private String  port= null;
    private String community= null;
    private int version;

    private Form form;

    public SnmpMenager() {
    }

    public SnmpMenager(Form form){
        try {
            this.form = form;
            init();
            System.out.println("Listening for trap");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void init() throws IOException {
        setTrapAddress("10.0.1.1/1620");

        // configure Snmp object
        UdpAddress listenAddress = new UdpAddress(trapAddress);
        DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping(listenAddress);
        Snmp snmp = new Snmp(transport);

        //System.out.println("Listening for trap " + trapAddress);

        CommandResponder trapListener = new CommandResponder() {
            public synchronized void processPdu(CommandResponderEvent e) {
                PDU trap = e.getPDU();
                //System.out.println("Listening for trap");
                if (trap != null) {
                    readAndSaveTrap(trap);
                    //makeGetToAgent(receivedAgentAddress, receivedAgentId);
                }
            }
        };
        snmp.addCommandResponder(trapListener);

        transport.listen();

    }
    public void makeGetToAgent(String agentAddress, String agentId) {


        try {
            UdpAddress targetAddress = new UdpAddress(agentAddress);
            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString("public"));
            target.setAddress(targetAddress);
            target.setRetries(2);
            target.setTimeout(10000);
            target.setVersion(SnmpConstants.version2c);

            TransportMapping transport = new DefaultUdpTransportMapping();
            transport.listen();

            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID(".1.3.6.1.2.1.1.1.0")));
            pdu.setType(PDU.GET);
            Snmp snmp = new Snmp(transport);

            ResponseEvent responseEvent = snmp.get(pdu, target);
            if (responseEvent != null) {
                //got a valid response!
                readAndSaveGetResult(responseEvent.getResponse(), agentId);
            }
            else {

            }
        }
        catch (IOException e) {

        }
    }
    public void readAndSaveGetResult(PDU respPDU, String agentId) {
        Vector<? extends VariableBinding> varVector = (Vector<? extends VariableBinding>) respPDU.getVariableBindings();
        String response = "";
        for (VariableBinding binding : varVector) {
            if (binding.getOid().equals(new OID(".1.3.6.1.2.1.1.1.0"))) {
                response = binding.getVariable().toString();
            }
        }
        String date = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS").format(new Date());
        System.out.println(date + " GET response from agent " + response);
    }

    public String getReceivedAgentAddress() {
        return receivedAgentAddress;
    }

    public void setReceivedAgentAddress(String receivedAgentAddress) {
        this.receivedAgentAddress = receivedAgentAddress;
    }

    public String getReceivedAgentId() {
        return receivedAgentId;
    }

    public void setReceivedAgentId(String receivedAgentId) {
        this.receivedAgentId = receivedAgentId;
    }

    public String getTrapAddress() {
        return trapAddress;
    }

    public void setTrapAddress(String trapAddress) {
        this.trapAddress = trapAddress;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getCommunity() {
        return community;
    }

    public void setCommunity(String community) {
        this.community = community;
    }
    public void readAndSaveTrap(PDU trap) {
        String date = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS").format(new Date());
        Variable alarmCode = trap.getVariable(new OID("1.2.3.4.5.1"));
        Variable agentId = trap.getVariable(new OID("1.2.3.4.5.2"));
        Variable alarmText = trap.getVariable(new OID("1.2.3.4.5.3"));
        Variable agentAddress = trap.getVariable(new OID("1.2.3.4.5.4"));
        String message = alarmCode.toString() + ":" + alarmText.toString();

        receivedAgentAddress = agentAddress.toString();
        receivedAgentId = agentId.toString();
        System.out.println(date + " TRAP from agent " + agentId.toString() +"("+ agentAddress.toString() +") "+ message);
        String trapMessage = date + " TRAP from agent " + agentId.toString() +"("+ agentAddress.toString() +") "+ message;
        form.setTrapMessage(trapMessage);
    }
    public VariableBinding get(String oid) {
        VariableBinding value;
        SnmpGet get = new SnmpGet();
        value = get.snmpGet(oid, this);
        return value;
    }

    public VariableBinding getnext(String oid) {
        VariableBinding value;
        SnmpGetNext getNext = new SnmpGetNext();
        value = getNext.snmpGetNext(oid, this);
        return value;
    }

    public List<VariableBinding> getbulk(String oid) {
        List<VariableBinding> listaVB;
        SnmpGetBulk GetBulk = new SnmpGetBulk();
        listaVB = GetBulk.snmpGetBulk(oid, this);
        return listaVB;
    }

    public List<VariableBinding> walk(String oid) throws IOException {
        List<VariableBinding> listaVB;
        SnmpWalk walk = new SnmpWalk();
        listaVB = walk.doWalk(oid, this);
        return listaVB;
    }

    public String set(Object value, String oid) {
        String saida="";
        SnmpSet set = new SnmpSet(oid,this);
        saida=set.set(value);
        return saida;
    }

    @Override
    public String toString() {
        return "SnmpMenager{" +
                "ip='" + ip + '\'' +
                ", port='" + port + '\'' +
                ", community='" + community + '\'' +
                ", version=" + version +
                '}';
    }
}


