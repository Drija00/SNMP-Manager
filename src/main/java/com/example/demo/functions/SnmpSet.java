package com.example.demo.functions;

import com.example.demo.menager.SnmpMenager;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class SnmpSet {
    private String ip, port, community, oid;
    private Snmp snmp = null;
    private PDU pdu = null;
    private OID Oid = null;
    private int version;
    private SnmpMenager menager=null;

    public SnmpSet(String oid, SnmpMenager menager) {
        this.menager=menager;
        this.ip=menager.getIp();
        this.port=menager.getPort();
        this.community=menager.getCommunity();
        this.oid=oid;
        this.version = menager.getVersion();
    }

    public String set(Object value) {
        String saida="";
        try {
            TransportMapping transport = new DefaultUdpTransportMapping();
            transport.listen();

            // criação do objeto alvo da conexão
            //A CommunityTarget represents SNMP target properties for community based message processing models (SNMPv1 and SNMPv2c).
            Address address = GenericAddress.parse("udp" + ":" + ip
                    + "/" + port);
            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString(community));
            target.setAddress(address);
            target.setVersion(version);
            target.setTimeout(1500); // milliseconds
            target.setRetries(2);
            // Criado PDU
            //The PDU class represents a SNMP protocol data unit.
            pdu = new PDU();
            Oid = new OID(oid);
            Variable var = null;
            if(value instanceof String) {
                var = new OctetString(value.toString());
            } else if(value instanceof Integer) {
                var = new Integer32(Integer.parseInt(value.toString()));
            }
            VariableBinding vb = new VariableBinding(Oid,var);
            pdu.add(vb);
            pdu.setType(PDU.SET);
            pdu.setRequestID(new Integer32(1));

            // Estabelecida conexão snmp com o objeto
            snmp = new Snmp(transport);


            //enviando request...


            ResponseEvent response = snmp.set(pdu, target);

            if (response != null) {
                //recebida a Response
                PDU responsePDU = response.getResponse();

                int errorStatus = responsePDU.getErrorStatus();
                int errorIndex = responsePDU.getErrorIndex();
                String errorStatusText = responsePDU.getErrorStatusText();

                if (errorStatus == PDU.noError){
                    //saida=responsePDU.getVariableBindings().get(0).getOid().toString();  - utilizar se quiser ler o OID
                    saida=responsePDU.getVariableBindings().get(0).getVariable().toString();
                } else{
//
                }
            }else{
//                System.out.println(e.getMessage());
            }

            snmp.close();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        saida=saida+"\n____________________________________________\n";
        return saida;
    }

}
