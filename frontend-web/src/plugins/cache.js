const sessionCache = {
  set(key, value) {
    if (!sessionStorage) {
      return
    }
    if (key != null && value != null) {
      sessionStorage.setItem(key, value)
    }
  },
  get(key) {
    if (!sessionStorage) {
      return null
    }
    if (key == null) {
      return null
    }
    return sessionStorage.getItem(key)
  },
  setJSON(key, jsonValue) {
    if (jsonValue != null) {
      this.set(key, JSON.stringify(jsonValue))
    }
  },
  getJSON(key) {
    const value = this.get(key)
    if (value != null) {
      try {
        return JSON.parse(value)
      } catch (e) {
        console.warn('[cache] JSON 解析失败', key, e)
        return null
      }
    }
    return null
  },
  remove(key) {
    sessionStorage.removeItem(key)
  }
}

const localCache = {
  set(key, value) {
    if (!localStorage) {
      return
    }
    if (key != null && value != null) {
      localStorage.setItem(key, value)
    }
  },
  get(key) {
    if (!localStorage) {
      return null
    }
    if (key == null) {
      return null
    }
    return localStorage.getItem(key)
  },
  setJSON(key, jsonValue) {
    if (jsonValue != null) {
      this.set(key, JSON.stringify(jsonValue))
    }
  },
  getJSON(key) {
    const value = this.get(key)
    if (value != null) {
      try {
        return JSON.parse(value)
      } catch (e) {
        console.warn('[cache] JSON 解析失败', key, e)
        return null
      }
    }
    return null
  },
  remove(key) {
    localStorage.removeItem(key)
  }
}

export default {
  /**
   * session 缓存
   */
  session: {
    /**
     * 设置 session 缓存
     * @param {string} key
     * @param {*} value
     */
    set(key, value) {
      sessionCache.set(key, value)
    },
    /**
     * 获取 session 缓存
     * @param {string} key
     * @returns {string|null}
     */
    get(key) {
      return sessionCache.get(key)
    },
    /**
     * 设置 session 缓存（JSON）
     * @param {string} key
     * @param {object} value
     */
    setJSON(key, value) {
      sessionCache.setJSON(key, value)
    },
    /**
     * 获取 session 缓存（JSON）
     * @param {string} key
     * @returns {object|null}
     */
    getJSON(key) {
      return sessionCache.getJSON(key)
    },
    /**
     * 移除 session 缓存
     * @param {string} key
     */
    remove(key) {
      sessionCache.remove(key)
    }
  },
  /**
   * local 缓存
   */
  local: {
    set(key, value) {
      localCache.set(key, value)
    },
    get(key) {
      return localCache.get(key)
    },
    setJSON(key, value) {
      localCache.setJSON(key, value)
    },
    getJSON(key) {
      return localCache.getJSON(key)
    },
    remove(key) {
      localCache.remove(key)
    }
  }
}
