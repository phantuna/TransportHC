import axios from 'axios';

const axiosClient = axios.create({
  baseURL: "https://api.anlaghien.io.vn",
  headers: {
    "Content-Type": "application/json"
  },
  withCredentials: true
});
axiosClient.interceptors.request.use((config) => {
  console.log("REQUEST URL:", config.baseURL + config.url);
  return config;
});
// Thêm token vào header trước khi gửi request đi
axiosClient.interceptors.request.use(
  (config) => {
    // Tên key lưu token trong localStorage của bạn (ví dụ: 'token' hoặc 'accessToken')
    const token = localStorage.getItem('token'); 
    
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Bắt lỗi 401 từ server trả về
axiosClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response && error.response.status === 401) {
      console.error("Token hết hạn hoặc không có quyền!");
      // Bạn có thể xử lý tự động xoá token và đẩy về trang đăng nhập ở đây
      // localStorage.clear();
      // window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default axiosClient;