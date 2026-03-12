import React, { useState, useEffect } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { 
  LayoutDashboard, 
  Users, 
  Truck, 
  Package, 
  FileText, 
  Settings, 
  LogOut,
  ChevronDown,
  BarChart3,
  Calendar
} from 'lucide-react';

const MainLayout = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const username = localStorage.getItem('username') || "User";

  // --- HÀM LẤY VÀ FORMAT ROLE TỪ LOCALSTORAGE ---
  const getDisplayRole = () => {
    try {
      // Đọc mảng permissions/roles mà bạn đã lưu lúc login
      // Bạn có thể sửa "permissions" thành "roles" nếu BE lưu dưới dạng roles
      const permsString = localStorage.getItem("permissions"); 
      if (!permsString) return "Tài xế"; // Mặc định nếu không thấy

      const perms = JSON.parse(permsString);
      if (!Array.isArray(perms)) return "Tài xế";

      // Kiểm tra xem trong mảng quyền có chứa các key đặc biệt không
      // Tùy theo cách Backend của bạn đặt tên, VD: 'ROLE_ADMIN', 'R_ADMIN', 'ADMIN'
      const isAdmin = perms.some(p => p.includes("R_ADMIN"));
      const isManager = perms.some(p => p.includes("R_MANAGER"));
      const isAccountant = perms.some(p => p.includes("R_ACCOUNTANT"));
      const isDriver = perms.some(p => p.includes("R_DRIVER"));

      if (isAdmin) return "Quản trị viên";
      if (isManager) return "Quản lý";
      if (isAccountant) return "Kế toán";
      if (isDriver) return "Tài xế";
      
      return "Tài xế";
0    } catch (error) {
      console.error("Lỗi parse role:", error);
      return "Tài xế";
    }
  };

  const userRole = getDisplayRole();

  const [isSettingsOpen, setIsSettingsOpen] = useState(true);

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('isAuthenticated');
    localStorage.removeItem('username');
    localStorage.removeItem('permissions');
    localStorage.removeItem('roles'); // Xóa thêm roles nếu có
    navigate('/');
  };

  const menuItems = [
    { label: 'Dashboard', path: '/dashboard', icon: <LayoutDashboard size={20} /> },
    { label: 'Quản lý kho', path: '/inventory', icon: <Package size={20} /> },
    { label: 'Quản lý chi phí', path: '/expenses', icon: <FileText size={20} /> },
    { label: 'Quản lý lịch trình', path: '/travels', icon: <Calendar size={20} /> },
    { label: 'Báo cáo', path: '/reports', icon: <BarChart3 size={20} /> },
    { 
      label: 'Cấu hình', 
      icon: <Settings size={20} />, 
      isSubmenu: true,
      children: [
        { label: 'Danh sách xe tải', path: '/trucks' },
        { label: 'Quản lý lái xe', path: '/drivers' },
        { label: 'Tài khoản hệ thống', path: '/accounts' },
        { label: 'DS hành trình', path: '/schedules' },
        { label: 'Loại chi phí', path: '/expense-types' },
      ]
    },
  ];

  useEffect(() => {
    const isChildActive = menuItems
      .find(m => m.isSubmenu)?.children
      .some(c => c.path === location.pathname);
      
    if (isChildActive) setIsSettingsOpen(true);
  }, [location.pathname]);

  let currentTitle = 'Trang chủ';
  for (const item of menuItems) {
    if (item.path === location.pathname) currentTitle = item.label;
    if (item.children) {
      const child = item.children.find(c => c.path === location.pathname);
      if (child) currentTitle = child.label;
    }
  }

  return (
    <div className="flex h-screen bg-slate-100 font-sans">
      {/* --- SIDEBAR --- */}
      <aside className="w-64 bg-slate-900 text-white flex flex-col transition-all duration-300">
        <div className="h-16 flex items-center px-6 border-b border-slate-700 cursor-pointer" onClick={() => navigate('/dashboard')}>
          <h1 className="text-xl font-bold italic tracking-wider">TransportHC</h1>
        </div>

        <nav className="flex-1 py-4 space-y-1 overflow-y-auto">
          {menuItems.map((item, index) => {
            if (item.isSubmenu) {
              const isActiveParent = item.children.some(c => c.path === location.pathname);
              return (
                <div key={index} className="flex flex-col">
                  <div
                    onClick={() => setIsSettingsOpen(!isSettingsOpen)}
                    className={`flex items-center justify-between px-6 py-3 cursor-pointer transition-colors ${
                      isActiveParent ? 'text-white' : 'text-slate-400 hover:text-white hover:bg-slate-800'
                    }`}
                  >
                    <div className="flex items-center">
                      <span className="mr-3">{item.icon}</span>
                      <span className="text-sm font-medium">{item.label}</span>
                    </div>
                    <ChevronDown size={16} className={`transition-transform duration-200 ${isSettingsOpen ? 'rotate-180' : ''}`} />
                  </div>
                  
                  <div className={`overflow-hidden transition-all duration-300 ${isSettingsOpen ? 'max-h-64' : 'max-h-0'}`}>
                    {item.children.map((child) => {
                      const isChildActive = location.pathname === child.path;
                      return (
                        <div
                          key={child.path}
                          onClick={() => navigate(child.path)}
                          className={`pl-14 pr-6 py-2.5 text-sm cursor-pointer transition-colors ${
                            isChildActive
                              ? 'bg-blue-500 text-white' 
                              : 'text-slate-400 hover:text-white hover:bg-slate-800'
                          }`}
                        >
                          {child.label}
                        </div>
                      );
                    })}
                  </div>
                </div>
              );
            }

            const isActive = location.pathname === item.path;
            return (
              <div
                key={item.path}
                onClick={() => navigate(item.path)}
                className={`flex items-center px-6 py-3 cursor-pointer transition-colors ${
                  isActive 
                    ? 'bg-blue-500 text-white' 
                    : 'text-slate-400 hover:bg-slate-800 hover:text-white'
                }`}
              >
                <span className="mr-3">{item.icon}</span>
                <span className="text-sm font-medium">{item.label}</span>
              </div>
            );
          })}
        </nav>

        <div className="p-4 border-t border-slate-700">
          <button onClick={handleLogout} className="flex items-center w-full text-slate-400 hover:text-red-400 transition-colors text-sm px-2 py-2 rounded-lg hover:bg-slate-800">
            <LogOut size={18} className="mr-2" /> Đăng xuất
          </button>
        </div>
      </aside>

      {/* --- MAIN CONTENT --- */}
      <div className="flex-1 flex flex-col overflow-hidden">
        <header className="h-16 bg-white shadow-sm flex items-center justify-between px-8 z-10">
          <h2 className="text-lg font-semibold text-slate-800">
            {currentTitle}
          </h2>
          
          <div className="flex items-center gap-3">
            <div className="text-right">
              <div className="text-sm font-bold text-slate-800">{username}</div>
              {/* --- ĐỔI HIỂN THỊ CHỖ NÀY --- */}
              <div className="text-xs text-slate-500 font-medium">{userRole}</div>
            </div>
            <div className="h-10 w-10 bg-blue-100 rounded-full flex items-center justify-center text-blue-700 font-bold uppercase border border-blue-200">
              {username.charAt(0)}
            </div>
          </div>
        </header>

        <main className="flex-1 overflow-y-auto p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default MainLayout;