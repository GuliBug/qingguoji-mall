import useUserStore from '@/store/modules/user'

/**
 * 操作权限指令：v-hasPermi="['*:*:*']"
 * 当前登录用户不具备该权限时移除元素
 */
export default {
  mounted(el, binding) {
    const { value } = binding
    const allPermission = '*:*:*'
    const permissions = useUserStore().permissions
    if (Array.isArray(value) && value.length > 0) {
      const permissionValues = value
      const hasPermission = permissions.some((p) => {
        return allPermission === p || permissionValues.includes(p)
      })
      if (!hasPermission) {
        el.parentNode && el.parentNode.removeChild(el)
      }
    } else {
      throw new Error(`请设置操作权限标签值`)
    }
  }
}
