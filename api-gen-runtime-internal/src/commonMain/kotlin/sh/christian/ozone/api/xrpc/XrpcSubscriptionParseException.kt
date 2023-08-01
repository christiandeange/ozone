package sh.christian.ozone.api.xrpc

import sh.christian.ozone.api.response.AtpErrorDescription

class XrpcSubscriptionParseException(
  val error: AtpErrorDescription?,
) : RuntimeException("Subscription result could not be parsed")
