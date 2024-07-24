import { NextAuthOptions } from "next-auth";
import NextAuth from "next-auth/next";
import KeycloakProvider from "next-auth/providers/keycloak";

import * as pino from "pino"

const log = pino.pino();

export const authOptions: NextAuthOptions = {
    providers: [
        KeycloakProvider({
            clientId: process.env.KEYCLOAK_ID,
            clientSecret: process.env.KEYCLOAK_SECRET,
            issuer: process.env.KEYCLOAK_ISSUER,
            authorization: { params: { scope: "openid email profile quote_sub" } },
        })
    ],
    callbacks: {
        jwt: async ({token,user,account,profile,trigger}) => {
            log.info("call jwt callback... [" + trigger + "]")
            if (account) {
                log.info(account)
                token.access_token = account?.access_token;
            }
            return token;
        },
        session: async({session, user, token}) => {
            log.info("call session callback...")
            session.user.access_token = token.access_token;
            session.user.subject = token.sub;
            return session;
        },
    },
    debug: false, //process.env.NEXTAUTH_DEBUG,
}

export const handler = NextAuth(authOptions);

export { handler as GET, handler as POST }