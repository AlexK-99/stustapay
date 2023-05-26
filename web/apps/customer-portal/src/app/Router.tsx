import * as React from "react";
import { createBrowserRouter, RouterProvider } from "react-router-dom";
import { ErrorPage } from "./ErrorPage";
import { AuthenticatedRoot } from "./routes/AuthenticatedRoot";
import { Login } from "./routes/auth/Login";
import { UnauthenticatedRoot } from "./routes/UnauthenticatedRoot";
import { Logout } from "./routes/auth/Logout";
import { Index } from "./routes/Index";
import { PayoutInfo } from "./routes/PayoutInfo";
import { TopUp } from "./routes/topup";

const router = createBrowserRouter([
  {
    path: "/",
    element: <AuthenticatedRoot />,
    errorElement: <ErrorPage />,
    children: [
      {
        path: "",
        element: <Index />,
      },
      {
        path: "payout-info",
        element: <PayoutInfo />,
      },
      {
        path: "topup",
        element: <TopUp />,
      },
      {
        path: "logout",
        element: <Logout />,
      },
    ],
  },
  {
    element: <UnauthenticatedRoot />,
    errorElement: <ErrorPage />,
    children: [
      {
        path: "/login",
        element: <Login />,
      },
    ],
  },
]);

export const Router: React.FC = () => {
  return <RouterProvider router={router} />;
};
