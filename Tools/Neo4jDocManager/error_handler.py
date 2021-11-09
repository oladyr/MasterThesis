import re
import logging

from mongo_connector import errors
from mongo_connector.errors import OperationFailed


LOG = logging.getLogger(__name__)

class Neo4jOperationFailed(OperationFailed):
    """Raised for failed commands on the destination database
    """
    # print("An error ocurred. Please check mongo-connector.log file")

class ErrorHandler(object):
  def __init__(self):
    self.error_hash = {   
    AttributeError: Neo4jOperationFailed,
    TypeError: Neo4jOperationFailed,
    NameError: Neo4jOperationFailed,
    RuntimeError: Neo4jOperationFailed
    
    }


