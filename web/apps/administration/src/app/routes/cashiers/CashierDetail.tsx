import * as React from "react";
import { Paper, ListItem, ListItemText, List, ListItemSecondaryAction, Typography } from "@mui/material";
import { DataGrid, GridColDef } from "@mui/x-data-grid";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router-dom";
import {
  selectCashierById,
  useGetCashierByIdQuery,
  selectTillById,
  useGetTillsQuery,
  useGetCashierShiftsQuery,
  selectUserById,
  useGetUsersQuery,
} from "@api";
import { Loading } from "@stustapay/components";
import { ButtonLink, IconButtonLink, ListItemLink } from "@components";
import { Edit as EditIcon } from "@mui/icons-material";
import { toast } from "react-toastify";
import { useCurrencyFormatter } from "@hooks";
import { CashierShift, getUserName } from "@stustapay/models";
import { formatDate } from "@stustapay/utils";

export const CashierDetail: React.FC = () => {
  const { t } = useTranslation(["cashiers", "common"]);
  const { cashierId } = useParams();
  const navigate = useNavigate();
  const formatCurrency = useCurrencyFormatter();

  const { cashier, error, isLoading } = useGetCashierByIdQuery(Number(cashierId), {
    selectFromResult: ({ data, ...rest }) => ({
      ...rest,
      cashier: data ? selectCashierById(data, Number(cashierId)) : undefined,
    }),
  });
  const {
    data: cashierShifts,
    error: shiftsError,
    isLoading: isShiftsLoading,
  } = useGetCashierShiftsQuery(Number(cashierId));
  const { data: tills, isLoading: isTillsLoading, error: tillError } = useGetTillsQuery();
  const { data: users, isLoading: isUsersLoading, error: userError } = useGetUsersQuery();

  if (error || tillError || shiftsError || userError) {
    navigate(-1);
    toast.error("Error while loading cashier");
    return null;
  }

  if (!cashier || !cashierShifts || isLoading || isTillsLoading || isShiftsLoading || isUsersLoading) {
    return <Loading />;
  }

  const getTill = (id: number) => {
    if (!tills) {
      return undefined;
    }
    return selectTillById(tills, id);
  };

  const renderUser = (id?: number | null) => {
    if (!id || !users) {
      return "";
    }

    const user = selectUserById(users, id);
    if (!user) {
      return "";
    }

    return getUserName(user);
  };

  const columns: GridColDef<CashierShift>[] = [
    {
      field: "id",
      headerName: t("shift.id") as string,
      // renderCell: (params) => <RouterLink to={`/cashiers/${params.row.id}`}>{params.row.login}</RouterLink>,
    },
    {
      field: "comment",
      headerName: t("shift.comment") as string,
      flex: 2,
    },
    {
      field: "closing_out_user_id",
      headerName: t("closeOut.closingOutUser") as string,
      type: "string",
      renderCell: (params) => renderUser(params.row.closing_out_user_id),
      width: 200,
    },
    {
      field: "started_at",
      headerName: t("shift.startedAt") as string,
      valueGetter: ({ value }) => formatDate(value),
      flex: 1,
    },
    {
      field: "ended_at",
      headerName: t("shift.endedAt") as string,
      valueGetter: ({ value }) => formatDate(value),
      flex: 1,
    },
    {
      field: "final_cash_drawer_balance",
      headerName: t("shift.finalCashDrawerBalance") as string,
      valueFormatter: ({ value }) => formatCurrency(value),
      type: "number",
    },
    {
      field: "final_cash_drawer_imbalance",
      headerName: t("shift.finalCashDrawerImbalance") as string,
      valueFormatter: ({ value }) => formatCurrency(value),
      type: "number",
    },
  ];

  return (
    <>
      <Paper>
        <ListItem
          secondaryAction={
            <>
              {cashier.cash_drawer_balance !== 0 && (
                <ButtonLink to={`/cashiers/${cashierId}/close-out`}>{t("cashier.closeOut")}</ButtonLink>
              )}
              <IconButtonLink to={`/cashiers/${cashierId}/edit`} color="primary" sx={{ mr: 1 }}>
                <EditIcon />
              </IconButtonLink>
            </>
          }
        >
          <ListItemText primary={getUserName(cashier)} />
        </ListItem>
      </Paper>
      <Paper sx={{ mt: 2 }}>
        <List>
          <ListItem>
            <ListItemText primary={t("cashier.login")} secondary={cashier.login} />
          </ListItem>
          <ListItem>
            <ListItemText primary={t("cashier.name")} secondary={cashier.display_name} />
          </ListItem>
          <ListItem>
            <ListItemText primary={t("cashier.description")} secondary={cashier.description} />
          </ListItem>
          <ListItem>
            <ListItemText primary={t("cashier.tagId")} secondary={String(cashier.user_tag_uid)} />
          </ListItem>
          {cashier.till_id ? (
            <ListItemLink to={`/tills/${getTill(cashier.till_id)?.id}`}>
              <ListItemText primary={t("cashier.till")} secondary={getTill(cashier.till_id)?.name} />
            </ListItemLink>
          ) : (
            <ListItem>
              <ListItemText primary={t("cashier.till")} secondary={t("cashier.notLoggedInAtTill")} />
            </ListItem>
          )}
          <ListItem>
            <ListItemText
              primary={t("cashier.cashDrawerBalance")}
              secondary={formatCurrency(cashier.cash_drawer_balance)}
            />
            {cashier.cash_drawer_balance !== 0 && (
              <ListItemSecondaryAction>
                <ButtonLink to={`/cashiers/${cashierId}/close-out`}>{t("cashier.closeOut")}</ButtonLink>
              </ListItemSecondaryAction>
            )}
          </ListItem>
        </List>
      </Paper>
      <Paper sx={{ mt: 2, p: 1 }}>
        <Typography variant="body1" sx={{ p: 1 }}>
          {t("cashier.shifts")}
        </Typography>
        <DataGrid
          autoHeight
          rows={cashierShifts}
          columns={columns}
          disableRowSelectionOnClick
          sx={{ border: "none" }}
        />
      </Paper>
    </>
  );
};
