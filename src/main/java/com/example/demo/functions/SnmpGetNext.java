package com.example.demo.functions;

import com.example.demo.menager.SnmpMenager;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;

public class SnmpGetNext {

    public static final int DEFAULT_VERSION = SnmpConstants.version2c;
    public static final String DEFAULT_PROTOCOL = "udp";
    public static final int DEFAULT_PORT = 161;
    public static final long DEFAULT_TIMEOUT = 3 * 1000L;
    public static final int DEFAULT_RETRY = 3;

    private String res;
    private String myOid;
    private VariableBinding vb;

    /**
     * 创建对象communityTarget
     *
     * @param ip
     * @param community
     * @return CommunityTarget
     */
    public static CommunityTarget createDefault(String ip, String community, String port, int version) {
        Address address = GenericAddress.parse(DEFAULT_PROTOCOL + ":" + ip
                + "/" + port);
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(community));
        target.setAddress(address);
        target.setVersion(version);
        target.setTimeout(1500); // milliseconds
        target.setRetries(2);
        return target;
    }

    public VariableBinding snmpGetNext(String oid, SnmpMenager menager) {
        CommunityTarget target = createDefault(menager.getIp(), menager.getCommunity(), menager.getPort(), menager.getVersion());
        Snmp snmp = null;
        try {
            PDU pdu = new PDU();
            // pdu.add(new VariableBinding(new OID(new int[]
            // {1,3,6,1,2,1,1,2})));
            pdu.add(new VariableBinding(new OID(oid)));

            DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            snmp.listen();
            pdu.setType(PDU.GETNEXT);
            ResponseEvent respEvent = snmp.send(pdu, target);
            //System.out.println("PeerAddress:" + respEvent.getPeerAddress());
            PDU response = respEvent.getResponse();

            if (response == null) {
                System.out.println("response is null, request time out");
            } else {

                // Vector<VariableBinding> vbVect =
                // response.getVariableBindings();
                // System.out.println("vb size:" + vbVect.size());
                // if (vbVect.size() == 0) {
                // System.out.println("response vb size is 0 ");
                // } else {
                // VariableBinding vb = vbVect.firstElement();
                // System.out.println(vb.getOid() + " = " + vb.getVariable());
                // }

                //System.out.println("response pdu size is " + response.size());
                for (int i = 0; i < response.size(); i++) {
                    vb = response.get(i);
                    //System.out.println("U GetNext-u  "+vb.getOid() + " = " + vb.getVariable());
                    //myOid = vb.getOid().toString();
                    //res = vb.getOid() + " = " + vb.getVariable();
                }

            }
            //System.out.println("SNMP GET one OID value finished !");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("SNMP Get Exception:" + e);
        } finally {
            if (snmp != null) {
                try {
                    snmp.close();
                } catch (IOException ex1) {
                    snmp = null;
                }
            }
            return vb;
        }
    }

    //@PostConstruct
//    public void main() {
//        String ip = "127.0.0.1";
//        String community = "public";
//        String oidval = ".1.3.6.1.2.1.1.1.0";
//        snmpGet(ip, community, oidval);
//
//    }


}
