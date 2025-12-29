package sh.christian.ozone.api.model.permissions

import sh.christian.ozone.api.Nsid
import sh.christian.ozone.api.Uri

/**
 * An individual permission that grants access via a resource.
 */
interface Permission {
  /**
   * The resource that this permission applies to.
   */
  val resource: PermissionResource
}

/**
 * Repository permissions, e.g. access to repos, collections, and actions within.
 */
data class RepoPermission(
  override val resource: PermissionResource,
  /**
   * The set of actions that are permitted.
   */
  val actions: List<String> = emptyList(),
  /**
   * The collections that can be actioned. This can either be a list of [Nsid]s, or the constant `'*'` to indicate that
   * all collections are in scope.
   */
  val collections: List<String> = emptyList(),
) : Permission

/**
 * RPC permissions, e.g. access to remote procedure calls.
 */
data class RpcPermission(
  override val resource: PermissionResource,
  /**
   * Whether the audience is inherited from a higher-level permission.
   */
  val inheritAudience: Boolean,
  /**
   * The audience that the permission applies to. This can either be a [Uri] or the constant `'*'` to indicate that all
   * audiences are in scope.
   */
  val audience: String? = null,
  /**
   * The methods that can be invoked. This can either be a list of [Nsid]s, or the constant `'*'` to indicate that all
   * methods are in scope.
   */
  val methods: List<String> = emptyList(),
) : Permission
