import useUserStore from '@/store/modules/user'

/**
 * 角色权限指令：v-hasRole="['admin']"
 * 当前登录用户不具备该角色时移除元素
 */
export default {
  mounted(el, binding) {
    const { value } = binding
    const roles = useUserStore().roles
    if (Array.isArray(value) && value.length > 0) {
      const permissionRoles = value
      const hasRole = roles.some((role) => permissionRoles.includes(role))
      if (!hasRole) {
        el.parentNode && el.parentNode.removeChild(el)
      }
    } else {
      throw new Error(`请设置角色权限标签值`)
    }
  }
}
