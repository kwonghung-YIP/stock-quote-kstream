import mqtt from "mqtt";
import * as pino from "pino"
import { useSession } from "next-auth/react"
import { useEffect } from "react";

const log = pino.pino();

export const MqttWrapper = () => {
    const session = useSession({required:true});

    log.info("outside useEffect");
    log.info(session.data.user.access_token);

    useEffect(() => {
        log.info("within useEffect");
        log.info(session.data?.user.access_token);
        //log.info("username:"+session?.user?.subject);
        //log.info("password:"+session?.user?.access_token);
        
        const { subject, access_token } = session.data?.user;

        if (subject && access_token) {
            const client = mqtt.connect(
                "ws://192.168.19.134:8000/mqtt",
                {
                    username: subject,
                    password: access_token,
                    protocolVersion: 5,
                    manualConnect: true,
                });

            client.on("connect",(connack) => {
                log.info("mqtt client connect")
            })
        
            client.on("reconnect",() => {
                log.info("mqtt client reconnect")
            })
        
            client.on("error",(error) => {
                log.info(`mqtt client error ${error}`)
            })
        
            client.on("disconnect",(packet) => {
                log.info("mqtt client disconnect")
            })
        
            client.on("offline",() => {
                log.info("mqtt client offline")
            })
        
            client.on("close",() => {
                log.info("mqtt client close")
            })
        
            client.on("end",() => {
                log.info("mqtt client end")
            })

            client.on("message",(topic,data,packet)=>{
                var msg = new String(data).toString();
                log.info("received message {} from topic {}",msg,topic);
            })

            client.on("packetsend",(packet)=>{
                log.info("packet sent: {}" + JSON.stringify(packet));
            })

            client.on("packetreceive",(packet)=>{
                log.info("packet received: {}" + JSON.stringify(packet));
            })

            log.info(`client.connect()...`)
            client.connect();

            client.subscribe('quote/111');

            return () => {
                client.end();
            }
        }
    },[session])

/*
    const client = mqtt.connect(
        "ws://192.168.19.134:8000/mqtt",
        {
            username: session?.user?.subject,
            password: session?.user?.access_token,
            protocolVersion: 5,
            manualConnect: true,
        });

        client.on("connect",(connack) => {
            log.info("mqtt client connect")
        })
    
        client.on("reconnect",() => {
            log.info("mqtt client reconnect")
        })
    
        client.on("error",(error) => {
            log.info(`mqtt client error ${error}`)
        })
    
        client.on("disconnect",(packet) => {
            log.info("mqtt client disconnect")
        })
    
        client.on("offline",() => {
            log.info("mqtt client offline")
        })
    
        client.on("close",() => {
            log.info("mqtt client close")
        })
    
        client.on("end",() => {
            log.info("mqtt client end")
        })
    
        log.info(`client.connect()...`)
        client.connect();
*/
    return (
        <p>{JSON.stringify(session)}</p>
    )
}