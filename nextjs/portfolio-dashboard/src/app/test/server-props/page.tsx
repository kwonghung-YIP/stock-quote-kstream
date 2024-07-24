import { GetServerSideProps, InferGetServerSidePropsType } from "next";
import * as pino from "pino"

const log = pino.pino();

type Props = {
    msg: string;
}
/*
export const getServerSideProps = (async () => {
    log.info("getServerSideProps")
    const config:Props = { msg:"text from me!!!" }
    return { props: { config } }
}) satisfies GetServerSideProps<{ config: Props }>
*/
export default function Page(/*{
    config
}: InferGetServerSidePropsType<typeof getServerSideProps>*/) {
    log.info("page","with default page function")

    return (
        <main>
            <p>getServerSideProps example page!!</p>
        </main>
    )
} 