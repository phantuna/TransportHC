import React from 'react';
import { useNavigate } from 'react-router-dom';

const Dashboard = () => {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem('token');
    navigate('/'); // Quay về trang login
  };

  return (
    <div className="min-h-screen bg-slate-50 p-8">
      <div className="max-w-7xl mx-auto">
        <div className="flex justify-between items-center mb-8">
          <h1 className="text-3xl font-bold text-slate-800">Dashboard Quản Lý</h1>
          <button 
            onClick={handleLogout}
            className="bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded-lg transition-colors"
          >
            Đăng xuất
          </button>
        </div>
        
        <div className="bg-white p-6 rounded-xl shadow-sm border border-slate-200">
          <p className="text-lg text-slate-600">
            Xin chào! Bạn đã đăng nhập thành công vào hệ thống TransportHC.
          </p>
          {/* Sau này bảng danh sách xe sẽ đặt ở đây */}
        </div>
      </div>
    </div>
  );
};

export default Dashboard;