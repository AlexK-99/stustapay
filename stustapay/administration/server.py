import argparse
import asyncio
import json
import logging

from stustapay.core import database
from stustapay.core.config import Config
from stustapay.core.http.context import Context
from stustapay.core.http.server import Server
from stustapay.core.service.account import AccountService
from stustapay.core.service.cashier import CashierService
from stustapay.core.service.config import ConfigService
from stustapay.core.service.order import OrderService
from stustapay.core.service.product import ProductService
from stustapay.core.service.tax_rate import TaxRateService
from stustapay.core.service.ticket import TicketService
from stustapay.core.service.till import TillService
from stustapay.core.service.user import AuthService, UserService
from stustapay.core.subcommand import SubCommand

from ..core.healthcheck import run_healthcheck
from .routers import account, auth, cashier
from .routers import config as config_router
from .routers import (
    order,
    product,
    stats,
    tax_rate,
    ticket,
    till,
    till_button,
    till_layout,
    till_profile,
    till_register_stockings,
    till_registers,
    user,
)


class Api(SubCommand):
    def __init__(self, args, config: Config, **rest):
        del rest  # unused
        self.args = args

        self.cfg = config
        self.dbpool = None

        self.logger = logging.getLogger(__name__)

        self.server = Server(
            title="StuStaPay Administration API",
            config=config.administration,
            cors=True,
        )

        self.server.add_router(product.router)
        self.server.add_router(user.router)
        self.server.add_router(tax_rate.router)
        self.server.add_router(auth.router)
        self.server.add_router(till.router)
        self.server.add_router(till_layout.router)
        self.server.add_router(till_profile.router)
        self.server.add_router(till_button.router)
        self.server.add_router(till_register_stockings.router)
        self.server.add_router(till_registers.router)
        self.server.add_router(config_router.router)
        self.server.add_router(account.router)
        self.server.add_router(order.router)
        self.server.add_router(cashier.router)
        self.server.add_router(stats.router)
        self.server.add_router(ticket.router)

    @staticmethod
    def argparse_register(subparser: argparse.ArgumentParser):
        subparser.add_argument(
            "--show-openapi",
            action="store_true",
        )

    async def run(self):
        if self.args.show_openapi:
            print(json.dumps(self.server.get_openapi_spec()))
            return

        db_pool = await self.server.db_connect(self.cfg.database)
        await database.check_revision_version(db_pool)

        auth_service = AuthService(db_pool=db_pool, config=self.cfg)
        product_service = ProductService(db_pool=db_pool, config=self.cfg, auth_service=auth_service)
        till_service = TillService(db_pool=db_pool, config=self.cfg, auth_service=auth_service)
        order_service = OrderService(db_pool=db_pool, config=self.cfg, auth_service=auth_service)

        context = Context(
            config=self.cfg,
            db_pool=db_pool,
            product_service=product_service,
            tax_rate_service=TaxRateService(db_pool=db_pool, config=self.cfg, auth_service=auth_service),
            user_service=UserService(db_pool=db_pool, config=self.cfg, auth_service=auth_service),
            till_service=till_service,
            config_service=ConfigService(db_pool=db_pool, config=self.cfg, auth_service=auth_service),
            account_service=AccountService(db_pool=db_pool, config=self.cfg, auth_service=auth_service),
            cashier_service=CashierService(db_pool=db_pool, config=self.cfg, auth_service=auth_service),
            order_service=order_service,
            ticket_service=TicketService(db_pool=db_pool, config=self.cfg, auth_service=auth_service),
        )
        try:
            self.server.add_task(asyncio.create_task(run_healthcheck(db_pool=db_pool, service_name="administration")))
            await self.server.run(self.cfg, context)
        finally:
            await db_pool.close()
