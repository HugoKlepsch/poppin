"""
Module for deletion of old events
"""
from datetime import datetime, timedelta
import threading
import time

from api_src.models import Event, Hype, Checkin


class GarbageCollector(threading.Thread):
    """Garbage Collector class"""
    def __init__(self, db, app, collection_period=timedelta(hours=1), retention_period=timedelta(hours=24)):
        """
        Constructor
        :param db: Database connection
        :param app_ctx: Flask app
        :param timedelta collection_period: How often the collection occurs
        :param retention_period: How long to keep events
        """
        self._db = db
        self._app_ctx = app.app_context
        self._logger = app.logger
        self.collection_period = collection_period
        self.retention_period = retention_period
        super().__init__()

    def run(self):
        """GC thread start"""
        with self._app_ctx():
            cycle_start_time = datetime.utcnow()
            self._logger.info("Garbage collector startup. Period: {period}".format(period=self.collection_period))
            while True:
                self._logger.info("Performing garbage collection...")
                self.remove_old_objects()
                next_cycle_start_time = cycle_start_time + self.collection_period
                self._logger.info("Done. Next cycle at {time}, sleeping".format(time=next_cycle_start_time))
                time.sleep((next_cycle_start_time - datetime.utcnow()).total_seconds())
                cycle_start_time = datetime.utcnow()

    def remove_old_objects(self):
        """Remove old events, hypes, checkins"""
        cutoff_date = datetime.utcnow() - self.retention_period
        Event.query.filter(Event.create_date < cutoff_date).delete()
        Hype.query.filter(Hype.create_date < cutoff_date).delete()
        Checkin.query.filter(Checkin.create_date < cutoff_date).delete()
        self._db.session.commit()
