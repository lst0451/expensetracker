<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h2>Expense Tracker</h2>
        <p>Please sign in to continue</p>
      </div>

      <form @submit.prevent="login" class="login-form">
        <div class="form-group">
          <label for="username">Username</label>
          <div class="input-wrapper">
            <i class="icon user-icon"></i>
            <input
                id="username"
                v-model="username"
                placeholder="Enter your username"
                required
                autocomplete="username"
            />
          </div>
        </div>

        <div class="form-group">
          <label for="password">Password</label>
          <div class="input-wrapper">
            <i class="icon lock-icon"></i>
            <input
                id="password"
                v-model="password"
                type="password"
                placeholder="Enter your password"
                required
                autocomplete="current-password"
            />
          </div>
        </div>

        <div class="form-options">
          <label class="remember-me">
            <input type="checkbox" v-model="rememberMe">
            <span>Remember me</span>
          </label>
          <a href="#" class="forgot-password">Forgot password?</a>
        </div>

        <button type="submit" class="login-button">
          Sign In
          <i class="icon arrow-icon"></i>
        </button>
      </form>

      <div class="login-footer">
        <p>Don't have an account? <a href="#">Sign up</a></p>
      </div>
    </div>
  </div>
</template>

<script>
//import axios from 'axios';

export default {
  data() {
    return {
      username: '',
      password: '',
      rememberMe: false
    };
  },
  methods: {
    login() {
      localStorage.removeItem('auth');

      const credentials = btoa(`${this.username}:${this.password}`);
      localStorage.setItem('auth', credentials);

      // If "Remember me" is selected, set additional flag
      if (this.rememberMe) {
        localStorage.setItem('rememberUser', 'true');
      } else {
        localStorage.removeItem('rememberUser');
      }

      this.$router.push('/expenses');
    }
  }
};
</script>

<style scoped>
/* 全局样式重置 */
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

/* 登录容器 */
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

/* 登录卡片 */
.login-card {
  width: 100%;
  max-width: 420px;
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  padding: 40px;
  transition: transform 0.3s ease;
}

.login-card:hover {
  transform: translateY(-5px);
}

/* 登录头部 */
.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  color: #333;
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 8px;
}

.login-header p {
  color: #888;
  font-size: 16px;
}

/* 表单样式 */
.login-form {
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: #555;
  font-weight: 500;
}

.input-wrapper {
  position: relative;
}

.icon {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  left: 12px;
  width: 20px;
  height: 20px;
  opacity: 0.5;
}

/* 使用伪元素作为图标 */
.user-icon:before {
  content: '👤';
}

.lock-icon:before {
  content: '🔒';
}

.arrow-icon:before {
  content: '→';
}

input {
  width: 100%;
  padding: 14px 14px 14px 40px;
  border: 2px solid #e1e1e1;
  border-radius: 8px;
  font-size: 15px;
  transition: all 0.3s;
  outline: none;
}

input:focus {
  border-color: #4a69bd;
  box-shadow: 0 0 0 3px rgba(74, 105, 189, 0.1);
}

input::placeholder {
  color: #bbb;
}

/* 选项区域 */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  font-size: 14px;
}

.remember-me {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.remember-me input {
  width: auto;
  margin-right: 6px;
}

.forgot-password {
  color: #4a69bd;
  text-decoration: none;
  transition: color 0.2s;
}

.forgot-password:hover {
  color: #2c3e50;
  text-decoration: underline;
}

/* 登录按钮 */
.login-button {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  padding: 14px;
  background-color: #4a69bd;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.3s;
  position: relative;
}

.login-button .icon {
  position: relative;
  left: 5px;
  top: 0;
  transform: none;
}

.login-button:hover {
  background-color: #3c5aa6;
}

.login-button:active {
  transform: translateY(1px);
}

/* 登录底部 */
.login-footer {
  text-align: center;
  margin-top: 30px;
  color: #777;
  font-size: 14px;
}

.login-footer a {
  color: #4a69bd;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.2s;
}

.login-footer a:hover {
  color: #2c3e50;
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .login-card {
    padding: 25px;
  }

  .login-header h2 {
    font-size: 24px;
  }
}
</style>