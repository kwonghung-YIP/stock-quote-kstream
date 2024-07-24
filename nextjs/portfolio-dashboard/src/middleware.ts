import { withAuth } from "next-auth/middleware"

export default withAuth(
    (req) => {
        //console.log(req);
    },
    {
        callbacks: {
            /*authorized: ({req,token}) => {
                //console.log('here 2:');
                //console.log(token);
                return true;
            },*/
        },
    }
)
export const config = {
    matcher: ["/portfolio/dashboard"]
}
