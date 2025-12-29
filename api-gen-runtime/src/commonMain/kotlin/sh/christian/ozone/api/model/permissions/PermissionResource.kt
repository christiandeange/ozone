package sh.christian.ozone.api.model.permissions

/**
 * A resource that permissions can apply to.
 */
data class PermissionResource(val name: String) {
  companion object {
    /**
     * Repository permissions, e.g. access to repos, collections, and actions within.
     */
    val Repo = PermissionResource("repo")

    /**
     * RPC permissions, e.g. access to remote procedure calls.
     */
    val Rpc = PermissionResource("rpc")
  }
}
