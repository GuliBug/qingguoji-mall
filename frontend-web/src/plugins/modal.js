import { ElMessage, ElMessageBox } from 'element-plus'

/**
 * 消息提示封装（若依 plugins/modal）
 */
const modal = {
  /**
   * 成功消息
   * @param {string} content
   */
  msgSuccess(content) {
    ElMessage({ message: content, type: 'success' })
  },
  /**
   * 错误消息
   * @param {string} content
   */
  msgError(content) {
    ElMessage({ message: content, type: 'error' })
  },
  /**
   * 确认对话框
   * @param {string} content 提示内容
   * @param {string} tip 标题
   * @returns {Promise<void>}
   */
  confirm(content, tip = '系统提示') {
    return ElMessageBox.confirm(content, tip, {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  }
}

export default modal
