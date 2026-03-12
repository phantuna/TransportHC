import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { toast } from "react-toastify";
import axiosClient from "../api/axiosClient";
import { User, Lock, Phone, Calendar, LogIn, UserPlus } from "lucide-react";

const AuthPage = () => {
  const [isLoginView, setIsLoginView] = useState(true);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    username: "",
    password: "",
    phone: "",
    birthday: ""
  });

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  // --- XỬ LÝ ĐĂNG NHẬP ---
  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const { data } = await axiosClient.post("/login", {
        username: formData.username,
        password: formData.password,
      });

      // Hỗ trợ cả 2 format: trả về thẳng data hoặc bọc trong data.result
      const result = data?.result || data;

      if (result?.token) {
        localStorage.setItem("token", result.token);
        localStorage.setItem("isAuthenticated", "true");
        localStorage.setItem("username", formData.username);
        
        // BẢO ĐẢM LƯU ĐÚNG MẢNG PERMISSIONS
        const userPermissions = result.permissions || [];
        localStorage.setItem("permissions", JSON.stringify(userPermissions));
        
        // ĐỂ BẠN DỄ DEBUG (Nhấn F12 -> Console để xem)
        console.log("✅ [LOGIN SUCCESS] Token:", result.token);
        console.log("🛡️ [PERMISSIONS] Quyền của tài khoản này là:", userPermissions);

        toast.success("Đăng nhập thành công!");
        navigate("/dashboard"); 
      } else {
        toast.error("Đăng nhập thất bại: Không nhận được Token!");
      }
    } catch (error) {
      console.error(error);
      toast.error(error?.response?.data?.message || "Sai tài khoản hoặc mật khẩu!");
    } finally {
      setLoading(false);
    }
  };

  // --- XỬ LÝ ĐĂNG KÝ ---
  const handleRegister = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      // Gọi API đăng ký (Thay đổi URL nếu Backend của bạn dùng đường dẫn khác)
      await axiosClient.post("/user/driver/created", {
        username: formData.username,
        password: formData.password,
        phone: formData.phone,
        birthday: formData.birthday
      });

      toast.success("Đăng ký thành công! Vui lòng đăng nhập.");
      setIsLoginView(true); // Chuyển về màn hình đăng nhập
    } catch (error) {
      console.error(error);
      toast.error(error?.response?.data?.message || "Đăng ký thất bại!");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-100 flex items-center justify-center p-4">
      <div className="bg-white w-full max-w-md rounded-2xl shadow-xl overflow-hidden">
        
        {/* TABS ĐIỀU HƯỚNG */}
        <div className="flex border-b border-slate-200">
          <button 
            onClick={() => setIsLoginView(true)}
            className={`flex-1 py-4 text-sm font-bold uppercase transition-colors ${isLoginView ? "text-blue-600 border-b-2 border-blue-600 bg-blue-50/50" : "text-slate-400 hover:bg-slate-50"}`}
          >
            Đăng nhập
          </button>
          <button 
            onClick={() => setIsLoginView(false)}
            className={`flex-1 py-4 text-sm font-bold uppercase transition-colors ${!isLoginView ? "text-blue-600 border-b-2 border-blue-600 bg-blue-50/50" : "text-slate-400 hover:bg-slate-50"}`}
          >
            Đăng ký
          </button>
        </div>

        <div className="p-8">
          <div className="text-center mb-8">
            <h2 className="text-2xl font-extrabold text-slate-800">
              {isLoginView ? "Chào mừng trở lại!" : "Tạo tài khoản mới"}
            </h2>
            <p className="text-slate-500 text-sm mt-2">
              Hệ thống Quản lý Vận tải & Kho bãi
            </p>
          </div>

          <form onSubmit={isLoginView ? handleLogin : handleRegister} className="space-y-5">
            {/* TÊN ĐĂNG NHẬP */}
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <User size={18} className="text-slate-400" />
              </div>
              <input 
                type="text" name="username" required
                placeholder="Tên đăng nhập"
                className="w-full pl-10 pr-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:bg-white outline-none transition-all"
                value={formData.username} onChange={handleChange}
              />
            </div>

            {/* MẬT KHẨU */}
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <Lock size={18} className="text-slate-400" />
              </div>
              <input 
                type="password" name="password" required
                placeholder="Mật khẩu"
                className="w-full pl-10 pr-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:bg-white outline-none transition-all"
                value={formData.password} onChange={handleChange}
              />
            </div>

            {/* TRƯỜNG DÀNH RIÊNG CHO ĐĂNG KÝ */}
            {!isLoginView && (
              <>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <Phone size={18} className="text-slate-400" />
                  </div>
                  <input 
                    type="text" name="phone" required
                    placeholder="Số điện thoại"
                    className="w-full pl-10 pr-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:bg-white outline-none transition-all"
                    value={formData.phone} onChange={handleChange}
                  />
                </div>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <Calendar size={18} className="text-slate-400" />
                  </div>
                  <input 
                    type="date" name="birthday" required
                    className="w-full pl-10 pr-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:bg-white outline-none transition-all text-slate-600"
                    value={formData.birthday} onChange={handleChange}
                  />
                </div>
              </>
            )}

            <button 
              type="submit" disabled={loading}
              className="w-full bg-slate-900 hover:bg-blue-600 text-white font-bold py-3.5 rounded-xl transition-colors flex items-center justify-center gap-2 mt-4 disabled:opacity-70"
            >
              {loading ? (
                <span className="animate-pulse">Đang xử lý...</span>
              ) : isLoginView ? (
                <><LogIn size={20} /> Đăng nhập</>
              ) : (
                <><UserPlus size={20} /> Đăng ký ngay</>
              )}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default AuthPage;