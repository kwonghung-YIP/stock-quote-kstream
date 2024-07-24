import { PortfolioDashboardApp } from "@/components/PortfolioDashboardApp";
import { getServerSession } from "next-auth"
import * as pino from "pino";

const log = pino.pino();

const Page = async () => {
    log.info("getServerSession...")

    const session = await getServerSession();

    return (
        <PortfolioDashboardApp session={session}>
        </PortfolioDashboardApp>
    )
}

export default Page