<template>
  <div class="register-container">
    <el-form ref="registerForm" :model="form" :rules="rules" class="register-form" auto-complete="on" label-position="left">
      <div class="title-container">
        <h3 class="title">快速注册</h3>
      </div>

      <el-form-item prop="username">
        <span class="svg-container">
          <svg-icon icon-class="user" />
        </span>
        <el-input
          ref="username"
          v-model="form.username"
          placeholder="Username"
          name="username"
          type="text"
          tabindex="1"
          auto-complete="on"
        />
      </el-form-item>

      <el-form-item prop="password">
        <span class="svg-container">
          <svg-icon icon-class="password" />
        </span>
        <el-input
          :key="passwordType"
          ref="password"
          v-model="form.password"
          :type="passwordType"
          placeholder="Password"
          name="password"
          tabindex="2"
          auto-complete="on"
        />
        <span class="show-pwd" @click="showPwd">
          <svg-icon :icon-class="passwordType === 'password' ? 'eye' : 'eye-open'" />
        </span>
      </el-form-item>

      <el-form-item prop="email">
        <span class="svg-container">
          <svg-icon icon-class="email" />
        </span>
        <el-input
          v-model="form.email"
          placeholder="Email"
          name="email"
          type="text"
          tabindex="3"
          auto-complete="on"
        />
      </el-form-item>

      <el-button
        type="primary"
        style="width: 100%; margin-bottom: 30px;"
        @click.native.prevent="submitForm"
      >
        注册
      </el-button>
    </el-form>
  </div>
</template>

<script>
import { register, checkUsername, checkEmail } from '@/api/user';

export default {
  name: 'Register',
  data() {
    return {
      form: {
        username: '',
        password: '',
        email: ''
      },
      rules: {
        username: [
          { required: true, message: 'Username is required', trigger: 'blur' },
          { min: 6, message: 'Username must be at least 6 characters', trigger: 'blur' },
          { validator: this.validateUsername, trigger: 'blur' } // 调用自定义校验器
        ],
        password: [
          { required: true, message: 'Password is required', trigger: 'blur' },
          { min: 6, message: 'Password must be at least 6 characters', trigger: 'blur' }
        ],
        email: [
          { required: true, message: 'Email is required', trigger: 'blur' },
          { type: 'email', message: 'Invalid email format', trigger: 'blur' },
          { validator: this.validateEmail, trigger: 'blur' } // 调用自定义校验器
        ]
      },
      passwordType: 'password'
    };
  },
  methods: {
    // 切换密码可见性
    showPwd() {
      this.passwordType = this.passwordType === 'password' ? '' : 'password';
      this.$nextTick(() => {
        this.$refs.password.focus();
      });
    },

    // 检查用户名是否存在
    async validateUsername(rule, value, callback) {
      if (!value) {
        return callback(new Error('Username is required'));
      }
      if (value.length < 6) {
        return callback(new Error('Username must be at least 6 characters'));
      }

      try {
        // 调用后端接口检查用户名是否存在
        const response = await checkUsername(value);
        console.log('Username check response:', response); // 调试日志

        // 确保解析 data.exists
        if (response.data.exists) {
          callback(new Error('Username already exists')); // 用户名已存在，阻止提交
        } else {
          callback(); // 验证通过
        }
      } catch (error) {
        console.error('Error validating username:', error); // 错误日志
        callback(new Error('Failed to validate username. Please try again.'));
      }
    },

    async validateEmail(rule, value, callback) {
      if (!value) {
        return callback(new Error('Email is required'));
      }

      try {
        // 调用后端接口检查邮箱是否存在
        const response = await checkEmail(value);
        console.log('Email check response:', response); // 调试日志

        // 确保解析 data.exists
        if (response.data.exists) {
          callback(new Error('Email already registered')); // 邮箱已注册，阻止提交
        } else {
          callback(); // 验证通过
        }
      } catch (error) {
        console.error('Error validating email:', error); // 错误日志
        callback(new Error('Failed to validate email. Please try again.'));
      }
    },

    // 提交表单
    async submitForm() {
      this.$refs.registerForm.validate(async (valid) => {
        if (valid) {
          try {
            console.log('Submitting form:', this.form); // 调试日志
            const response = await register(this.form); // 调用注册接口
            if (response.success) {
              this.$message.success('注册成功！正在跳转到登录页面...');
              this.$router.push('/login'); // 跳转到登录页面
            } else {
              this.$message.error(response.message || '注册失败，请稍后重试');
            }
          } catch (error) {
            console.error('Error during registration:', error); // 错误日志
            this.$message.error('注册失败，请稍后重试');
          }
        } else {
          this.$message.error('请检查表单内容');
        }
      });
    }
  }
};
</script>


<style lang="scss">
/* 修复input 背景不协调 和光标变色 */
/* Detail see https://github.com/PanJiaChen/vue-element-admin/pull/927 */

$bg:#283443;
$light_gray:#fff;
$cursor: #fff;

@supports (-webkit-mask: none) and (not (cater-color: $cursor)) {
  .login-container .el-input input {
    color: $cursor;
  }
}

/* reset element-ui css */
.register-container {
  .el-input {
    display: inline-block;
    height: 47px;
    width: 85%;

    input {
      background: transparent;
      border: 0px;
      -webkit-appearance: none;
      border-radius: 0px;
      padding: 12px 5px 12px 15px;
      color: $light_gray;
      height: 47px;
      caret-color: $cursor;

      &:-webkit-autofill {
        box-shadow: 0 0 0px 1000px $bg inset !important;
        -webkit-text-fill-color: $cursor !important;
      }
    }
  }

  .el-form-item {
    border: 1px solid rgba(255, 255, 255, 0.1);
    background: rgba(0, 0, 0, 0.1);
    border-radius: 5px;
    color: #454545;
  }
}
</style>

<style lang="scss" scoped>
$bg: #2d3a4b;
$dark_gray: #889aa4;
$light_gray: #eee;

.register-container {
  min-height: 100%;
  width: 100%;
  background-color: $bg;
  overflow: hidden;

  .register-form {
    position: relative;
    width: 520px;
    max-width: 100%;
    padding: 160px 35px 0;
    margin: 0 auto;
    overflow: hidden;
  }

  .svg-container {
    padding: 6px 5px 6px 15px;
    color: $dark_gray;
    vertical-align: middle;
    width: 30px;
    display: inline-block;
  }

  .title-container {
    position: relative;

    .title {
      font-size: 26px;
      color: $light_gray;
      margin: 0 auto 40px;
      text-align: center;
      font-weight: bold;
    }
  }

  .show-pwd {
    position: absolute;
    right: 10px;
    top: 7px;
    font-size: 16px;
    color: $dark_gray;
    cursor: pointer;
    user-select: none;
  }

  .login-button {
    display: block; /* Button occupies full width */
    margin: 0 auto; /* Center button horizontally */
    text-align: center;
    color: #409EFF; /* Button color */
    font-size: 14px; /* Font size */
    font-weight: bold;

    &:hover {
      text-decoration: underline; /* Add underline on hover */
    }
  }

  .el-input {
    display: inline-block;
    height: 47px;
    width: 85%;

    input {
      background: transparent;
      border: 0;
      -webkit-appearance: none;
      border-radius: 0;
      padding: 12px 5px 12px 15px;
      color: $light_gray;
      height: 47px;
      caret-color: $light_gray;

      &:-webkit-autofill {
        box-shadow: 0 0 0px 1000px $bg inset !important;
        -webkit-text-fill-color: $light_gray !important;
      }
    }
  }

  .el-form-item {
    border: 1px solid rgba(255, 255, 255, 0.1);
    background: rgba(0, 0, 0, 0.1);
    border-radius: 5px;
    color: #454545;
  }
}
</style>
