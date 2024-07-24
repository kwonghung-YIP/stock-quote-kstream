"use client"

import { SessionProvider } from "next-auth/react"
import { MqttWrapper } from "./MqttWrapper"
import { PropsWithChildren } from "react"
import { Session } from "next-auth"

export type AppConfig = {
    session: Session | null
}

export function PortfolioDashboardApp({
    session, children
}: PropsWithChildren<AppConfig>) {
    return (
        <SessionProvider session={session}>
            <p>
                Portfolio Dashboard App!!!
            </p>
            <MqttWrapper/>
        </SessionProvider>
    )
}