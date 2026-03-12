import React, { useEffect, useState, useCallback } from "react";
import {
  Edit, Trash2, Plus, ChevronLeft, ChevronRight, Users, XCircle, Save, AlertTriangle, ShieldAlert
} from "lucide-react";
import { toast } from "react-toastify";
import moment from "moment";
import axiosClient from "../api/axiosClient";
import { hasPermission } from "../utils/auth";

const DriversPage = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);

  // Phân trang
  const [pagination, setPagination] = useState({
    page: 0,
    size: 25,
    total: 0,
    totalPages: 1,
  });

  // State Modals
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalMode, setModalMode] = useState("create");
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [itemToDelete, setItemToDelete] = useState(null);

  const [formData, setFormData] = useState({
    id: "",
    username: "",
    password: "",
    phone: "",
    birthday: "",
  });

  // --- LOGIC PHÂN QUYỀN (Khớp với backend PermissionKey.MANAGE/VIEW) ---
  const canManage = hasPermission("USER_MANAGE") || hasPermission("MANAGE_USER") || hasPermission("ADMIN");
  const canView = hasPermission("USER_VIEW") || hasPermission("VIEW_USER") || canManage;

  // Lấy username hiện tại từ localStorage để so sánh (ẩn nút sửa nếu không phải chính mình)
  const currentUsername = localStorage.getItem("username");

  // --- HÀM FORMAT TÊN ROLE BAO CHUẨN ---
  const formatRoleName = (user) => {
    let roleStr = "DRIVER";

    if (user?.roles && user.roles.length > 0) {
      roleStr = typeof user.roles[0] === 'string' ? user.roles[0] : (user.roles[0].name || "DRIVER");
    } else if (user?.role) {
      roleStr = typeof user.role === 'string' ? user.role : (user.role.name || "DRIVER");
    }

    const cleanName = roleStr.replace("ROLE_", "").replace("R_", "").toUpperCase();

    if (cleanName === "ADMIN") return "Quản trị viên";
    if (cleanName === "MANAGER") return "Quản lý";
    return "Tài xế";
  };

  // --- 1. FETCH DATA ---
  const fetchDrivers = useCallback(async (page = 0) => {
    if (!canView) return;
    setLoading(true);
    try {
      if (canManage) {
        // Có quyền MANAGE -> Gọi API lấy tất cả
        const res = await axiosClient.get("/user/getAll", {
          params: { page, size: pagination.size },
        });
        const data = res.data?.result || res.data; // Tương thích PageResponse

        setUsers(data.content || []);
        setPagination(prev => ({
          ...prev,
          page: data.page ?? data.number ?? page,
          total: data.totalElements ?? 0,
          totalPages: data.totalPages ?? 1,
        }));
      } else {
        // Chỉ có VIEW -> Trả về thông tin cá nhân (driver/view)
        const res = await axiosClient.get("/user/driver/view");
        const myProfile = res.data?.result || res.data;
        setUsers(myProfile ? [myProfile] : []);
        setPagination(prev => ({ ...prev, page: 0, total: 1, totalPages: 1 }));
      }
    } catch (error) {
      toast.error("Không thể tải danh sách người dùng!");
    } finally {
      setLoading(false);
    }
  }, [canManage, canView, pagination.size]);

  useEffect(() => {
    fetchDrivers(0);
  }, [fetchDrivers]);

  // --- 2. CREATE & UPDATE ---
  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      if (modalMode === "create") {
        // Backend: POST /user/driver/created
        const payload = {
          username: formData.username,
          password: formData.password,
          phone: formData.phone,
          birthday: formData.birthday,
        };
        await axiosClient.post("/user/driver/created", payload);
        toast.success("Tạo tài khoản tài xế thành công!");
      } else {
        // Backend: PUT /user/update (Update My Profile - Không truyền ID)
        const payload = {
          username: formData.username,
          phone: formData.phone,
          birthday: formData.birthday,
        };
        await axiosClient.put(`/user/update`, payload);
        toast.success("Cập nhật hồ sơ thành công!");

        // Nếu cập nhật username, cần set lại localStorage
        if (formData.username && formData.username !== currentUsername) {
          localStorage.setItem("username", formData.username);
        }
      }
      setIsModalOpen(false);
      fetchDrivers(canManage ? pagination.page : 0);
    } catch (error) {
      toast.error(error?.response?.data?.message || "Thao tác thất bại!");
    }
  };

  // --- 3. DELETE ---
  const handleDeleteClick = (id) => {
    setItemToDelete(id);
    setIsDeleteModalOpen(true);
  };
  const confirmDelete = async () => {
    if (!itemToDelete) return;

    try {
      await axiosClient.delete(`/user/delete/${itemToDelete}`);
      toast.success("Đã xóa tài xế thành công!");
      setIsDeleteModalOpen(false);
      setItemToDelete(null);
      fetchDrivers(pagination.page);
    } catch (error) {
      toast.error(error?.response?.data?.message || "Xóa thất bại! Dữ liệu đang bị ràng buộc.");
    }
  };

  // Sửa lại state một chút
  const [drivers, setDrivers] = useState([]);

  // Hàm gọi API lấy tài xế rảnh
  const fetchAvailableDrivers = async (truckId = "") => {
    try {
      const response = await axiosClient.get("/user/drivers/available", {
        params: { truckId: truckId }
      });
      setDrivers(response.data?.result || response.data || []);
    } catch (error) {
      console.error("Lỗi lấy danh sách tài xế", error);
    }
  };

  // Cập nhật hàm mở Modal Create
  const openCreateModal = () => {
    setModalMode("create");
    setFormData({ id: "", licensePlate: "", typeTruck: "", driverId: "", driverName: "", ganMooc: false, status: "AVAILABLE" });
    fetchAvailableDrivers(""); // Truyền rỗng để chỉ lấy người hoàn toàn rảnh
    setIsModalOpen(true);
  };

  // Cập nhật hàm mở Modal Edit
  const openEditModal = (truck) => {
    setModalMode("edit");
    setFormData({
      id: truck.id,
      licensePlate: truck.licensePlate,
      typeTruck: truck.typeTruck || "",
      driverId: truck.driverId || "",
      driverName: truck.driverName || "",
      ganMooc: truck.ganMooc || false,
      status: truck.status || "AVAILABLE",
    });
    fetchAvailableDrivers(truck.id); // Truyền ID xe để lấy người rảnh + tài xế hiện tại của xe này
    setIsModalOpen(true);
  };

  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < pagination.totalPages) fetchDrivers(newPage);
  };

  const renderPageNumbers = () => {
    const pages = [];
    let startPage = Math.max(0, pagination.page - 2);
    let endPage = Math.min(pagination.totalPages - 1, pagination.page + 2);
    for (let i = startPage; i <= endPage; i++) {
      pages.push(
        <button key={i} onClick={() => handlePageChange(i)}
          className={`w-8 h-8 rounded-md text-sm font-medium transition ${pagination.page === i ? "bg-blue-600 text-white shadow-sm" : "bg-white text-slate-600 border hover:bg-slate-50"
            }`}
        >
          {i + 1}
        </button>
      );
    }
    return pages;
  };

  if (!canView) {
    return (
      <div className="flex flex-col items-center justify-center h-64 bg-white rounded-xl shadow-sm border border-slate-200">
        <ShieldAlert size={48} className="text-amber-500 mb-3" />
        <h2 className="text-xl font-bold text-slate-800">Truy cập bị từ chối</h2>
        <p className="text-slate-500 mt-1">Tài khoản của bạn không có quyền xem trang này.</p>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-xl shadow-sm p-6 min-h-[600px] flex flex-col justify-between border border-slate-200">
      <div>
        {/* HEADER */}
        <div className="flex flex-col md:flex-row justify-between items-center mb-6 gap-4">
          <div>
            <h1 className="text-2xl font-extrabold text-slate-800 flex items-center tracking-tight">
              <div className="p-2 bg-blue-600 rounded-lg mr-3 shadow-md shadow-blue-200">
                <Users size={24} className="text-white" />
              </div>
              Quản lý Lái xe
            </h1>
            <p className="text-sm text-slate-500 mt-2 ml-14 font-medium">
              Bạn đang quản lý <span className="font-bold text-blue-600">{pagination.total}</span> nhân sự
            </p>
          </div>

          <div className="flex gap-2">
            {canManage && (
              <button onClick={openCreateModal} className="flex items-center px-4 py-2.5 bg-blue-600 text-white rounded-xl hover:bg-blue-700 text-sm font-bold transition shadow-lg shadow-blue-200 active:scale-95">
                <Plus size={18} className="mr-2" /> Thêm tài xế
              </button>
            )}
          </div>
        </div>

        {/* TABLE */}
        <div className="overflow-x-auto border border-slate-200 rounded-xl">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-slate-50/80 text-slate-500 text-[11px] uppercase tracking-wider border-b border-slate-200">
                <th className="px-6 py-4 font-bold">STT</th>
                <th className="px-6 py-4 font-bold">Tài khoản</th>
                <th className="px-6 py-4 font-bold">Số điện thoại</th>
                <th className="px-6 py-4 font-bold">Ngày sinh</th>
                <th className="px-6 py-4 font-bold">Vai trò</th>
                <th className="px-6 py-4 font-bold text-right">Thao tác</th>
              </tr>
            </thead>
            <tbody className="text-sm text-slate-700 divide-y divide-slate-100">
              {loading ? (
                <tr><td colSpan="6" className="text-center py-16 text-slate-400 font-medium animate-pulse">Đang tải dữ liệu...</td></tr>
              ) : users.length === 0 ? (
                <tr><td colSpan="6" className="text-center py-16 text-slate-400 font-medium">Chưa có dữ liệu</td></tr>
              ) : (
                users.map((user, index) => (
                  <tr key={user.id || index} className="hover:bg-blue-50/40 transition-colors group">
                    <td className="px-6 py-4 text-slate-400 font-medium">
                      {((pagination.page || 0) * (pagination.size || 10)) + index + 1}
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-3">
                        <div className="h-9 w-9 rounded-full bg-blue-100 text-blue-700 flex items-center justify-center font-bold text-xs border border-blue-200 uppercase">
                          {(user.username || "U").substring(0, 2)}
                        </div>
                        <span className="font-bold text-slate-800">{user.username}</span>
                      </div>
                    </td>
                    <td className="px-6 py-4 font-mono text-slate-600">{user.phone || "-"}</td>
                    <td className="px-6 py-4 text-slate-500">
                      {user.birthday ? moment(user.birthday).format('DD/MM/YYYY') : '-'}
                    </td>

                    {/* NÂNG CẤP HIỂN THỊ VAI TRÒ CHỐNG LỖI */}
                    <td className="px-6 py-4">
                      {((user.roles && user.roles.length > 0) || user.role) ? (
                        <span className="px-2.5 py-1 bg-emerald-50 text-emerald-700 border border-emerald-200 rounded-full text-[10px] font-black uppercase tracking-wider">
                          {formatRoleName(user)}
                        </span>
                      ) : (
                        <span className="px-2.5 py-1 bg-slate-100 text-slate-500 border border-slate-200 rounded-full text-[10px] font-black uppercase">N/A</span>
                      )}
                    </td>

                    <td className="px-6 py-4 text-right">
                      <div className="flex items-center justify-end gap-1 transition-opacity">
                        {/* Logic Ẩn Nút Edit: Chỉ cho phép tự Edit chính mình */}
                        {(!canManage || user.username === currentUsername) ? (
                          <button onClick={() => openEditModal(user)} className="p-2 text-slate-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition" title="Cập nhật hồ sơ">
                            <Edit size={16} />
                          </button>
                        ) : (
                          <span className="text-[10px] text-amber-500 font-medium px-2" title="API Backend chưa hỗ trợ cập nhật tài khoản khác">Chỉ Xem</span>
                        )}

                        {/* Nút Xóa: Chỉ hiện khi có quyền Quản lý */}
                        {canManage && (
                          <button onClick={() => handleDeleteClick(user.id)} className="p-2 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition" title="Xóa tài khoản">
                            <Trash2 size={16} />
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* --- PHÂN TRANG --- */}
      {canManage && pagination.totalPages > 1 && (
        <div className="flex flex-col sm:flex-row justify-between items-center mt-6 pt-4 border-t border-slate-100 gap-4">
          <div className="text-sm text-slate-500 font-medium">
            Trang {pagination.page + 1} / {pagination.totalPages}
          </div>
          <div className="flex items-center gap-2">
            <button onClick={() => handlePageChange(pagination.page - 1)} disabled={pagination.page === 0} className="p-1.5 border border-slate-200 rounded-lg text-slate-600 hover:bg-slate-50 disabled:opacity-30 transition">
              <ChevronLeft size={18} />
            </button>
            <div className="hidden sm:flex gap-1">
              {renderPageNumbers()}
            </div>
            <button onClick={() => handlePageChange(pagination.page + 1)} disabled={pagination.page >= pagination.totalPages - 1} className="p-1.5 border border-slate-200 rounded-lg text-slate-600 hover:bg-slate-50 disabled:opacity-30 transition">
              <ChevronRight size={18} />
            </button>
          </div>
        </div>
      )}

      {/* --- MODAL FORM --- */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 backdrop-blur-sm p-4">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md overflow-hidden animate-in zoom-in duration-200">
            <div className="bg-slate-800 px-6 py-4 flex justify-between items-center">
              <h3 className="text-white font-bold tracking-wide">
                {modalMode === 'create' ? 'THÊM TÀI XẾ MỚI' : 'HỒ SƠ CÁ NHÂN'}
              </h3>
              <button onClick={() => setIsModalOpen(false)} className="text-slate-400 hover:text-white transition">
                <XCircle size={22} />
              </button>
            </div>

            <form onSubmit={handleSubmit} className="p-6 space-y-4">
              {modalMode === 'edit' && canManage && (
                <div className="bg-blue-50 text-blue-700 p-3 rounded-lg text-xs font-medium mb-4 flex items-start gap-2">
                  <AlertTriangle size={16} className="shrink-0 mt-0.5" />
                  <p>Lưu ý: Hệ thống hiện tại chỉ hỗ trợ Cập nhật hồ sơ của chính bạn. Không thể sửa thông tin người khác.</p>
                </div>
              )}

              <div>
                <label className="block text-[11px] font-bold text-slate-500 mb-1 uppercase">Tên đăng nhập *</label>
                <input
                  type="text" required
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition"
                  value={formData.username}
                  onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                />
              </div>

              {modalMode === 'create' && (
                <div>
                  <label className="block text-[11px] font-bold text-slate-500 mb-1 uppercase">Mật khẩu *</label>
                  <input
                    type="password" required
                    placeholder="Nhập mật khẩu..."
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition"
                    value={formData.password}
                    onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                  />
                </div>
              )}

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-[11px] font-bold text-slate-500 mb-1 uppercase">Số điện thoại</label>
                  <input type="text"
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition font-mono"
                    value={formData.phone}
                    onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                  />
                </div>
                <div>
                  <label className="block text-[11px] font-bold text-slate-500 mb-1 uppercase">Ngày sinh</label>
                  <input type="date"
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition text-sm text-slate-600"
                    value={formData.birthday}
                    onChange={(e) => setFormData({ ...formData, birthday: e.target.value })}
                  />
                </div>
              </div>

              <div className="pt-4 flex justify-end gap-3">
                <button type="button" onClick={() => setIsModalOpen(false)}
                  className="px-5 py-2 border border-slate-200 rounded-lg text-slate-600 hover:bg-slate-50 font-bold text-sm transition">
                  HỦY
                </button>
                <button type="submit"
                  className="px-5 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-bold text-sm flex items-center shadow-lg shadow-blue-200 transition">
                  <Save size={18} className="mr-2" /> {modalMode === 'create' ? 'TẠO MỚI' : 'CẬP NHẬT'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* --- MODAL XÓA --- */}
      {isDeleteModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-sm p-4 animate-fade-in">
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-sm overflow-hidden p-6 text-center zoom-in duration-200">
            {/* Icon Cảnh báo */}
            <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <AlertTriangle size={32} className="text-red-600" />
            </div>

            {/* Tiêu đề & Nội dung */}
            <h3 className="text-xl font-bold text-slate-800 mb-2">Xác nhận xóa</h3>
            <p className="text-slate-500 text-sm mb-6">
              Hành động này sẽ xóa vĩnh viễn dữ liệu. Bạn có chắc chắn không?
            </p>

            {/* Nút bấm */}
            <div className="flex justify-center gap-3">
              <button
                onClick={() => {
                  setIsDeleteModalOpen(false);
                  setItemToDelete(null);
                }}
                className="px-5 py-2 border border-slate-200 rounded-lg hover:bg-slate-50 font-bold text-slate-600 text-sm transition"
              >
                HỦY
              </button>
              <button
                onClick={confirmDelete}
                className="px-5 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 font-bold text-sm transition shadow-lg shadow-red-200"
              >
                XÓA NGAY
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default DriversPage;