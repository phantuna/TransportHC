import React, { useEffect, useState, useCallback } from "react";
import {
  Edit, Trash2, Plus, Upload, ChevronLeft, ChevronRight, Truck, XCircle, Save, CheckCircle, AlertTriangle, ShieldAlert
} from "lucide-react";
import { toast } from "react-toastify";
import axiosClient from "../api/axiosClient";
import { hasPermission } from "../utils/auth";

const TrucksPage = () => {
  const [trucks, setTrucks] = useState([]);
  const [drivers, setDrivers] = useState([]); // Lưu danh sách tài xế cho Dropdown
  const [loading, setLoading] = useState(true);

  // Phân trang
  const [pagination, setPagination] = useState({
    page: 0,
    size: 25,
    total: 0,
    totalPages: 1,
  });

  // State Modal
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalMode, setModalMode] = useState("create");
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [itemToDelete, setItemToDelete] = useState(null);

  const [formData, setFormData] = useState({
    id: "",
    licensePlate: "",
    typeTruck: "",
    driverId: "",
    driverName: "",
    ganMooc: false,
    status: "AVAILABLE",
  });

  const canManage = hasPermission("MANAGE_TRUCK") || hasPermission("TRUCK_MANAGE") || hasPermission("ADMIN");
  const canView = hasPermission("VIEW_TRUCK") || hasPermission("TRUCK_VIEW") || canManage;

  // --- 1. LẤY DỮ LIỆU XE TẢI ---
  const fetchTrucks = useCallback(async (page = 0) => {
    if (!canView) return;
    setLoading(true);
    try {
      const { data } = await axiosClient.get("/truck/getAll", {
        params: { page, size: pagination.size },
      });
      const listData = data?.content || data?.result || [];
      setTrucks(listData);
      setPagination(prev => ({
        ...prev,
        page: data?.number ?? data?.page ?? page,
        total: data?.totalElements ?? listData.length,
        totalPages: data?.totalPages ?? 1,
      }));
    } catch (error) {
      toast.error("Không thể tải dữ liệu xe");
    } finally {
      setLoading(false);
    }
  }, [canView, pagination.size]);

  // --- 2. LẤY DANH SÁCH TÀI XẾ RẢNH (CHO DROPDOWN) ---
  const fetchAvailableDrivers = async (truckId = "") => {
    if (!canManage) return; 
    try {
      // Gọi đúng API bạn đã test trên Postman, truyền thêm truckId
      const response = await axiosClient.get("/user/drivers/available", {
        params: { truckId: truckId }
      });
      const data = response.data?.result || response.data || [];
      setDrivers(data);
    } catch (error) {
      console.error("Lỗi lấy danh sách tài xế", error);
    }
  };

  useEffect(() => {
    fetchTrucks(0);
    // Bỏ gọi danh sách tài xế ở đây, chỉ gọi khi nào người dùng MỞ FORM
  }, [fetchTrucks]);

  // --- 3. TẠO MỚI / CẬP NHẬT ---
  const handleSubmit = async (e) => {
    e.preventDefault();
    const payload = {
      licensePlate: formData.licensePlate,
      typeTruck: formData.typeTruck,
      driverId: formData.driverId || null,
      ganMooc: formData.ganMooc,
      status: formData.status,
    };

    try {
      if (modalMode === "create") {
        await axiosClient.post("/truck/created", payload);
        toast.success("Thêm xe mới thành công!");
      } else {
        await axiosClient.put(`/truck/updated/${formData.id}`, payload);
        toast.success("Cập nhật xe thành công!");
      }
      setIsModalOpen(false);
      fetchTrucks(pagination.page);
    } catch (error) {
      toast.error(error?.response?.data?.message || "Thao tác thất bại!");
    }
  };

  // --- 4. XỬ LÝ XÓA ---
  const handleDeleteClick = (id) => {
    setItemToDelete(id);
    setIsDeleteModalOpen(true);
  };

  const confirmDelete = async () => {
    if (!itemToDelete) return;
    try {
      await axiosClient.delete(`/truck/delete/${itemToDelete}`);
      toast.success("Xóa xe thành công!");
      setIsDeleteModalOpen(false);
      setItemToDelete(null);
      fetchTrucks(pagination.page);
    } catch (error) {
      toast.error(error?.response?.data?.message || "Xóa thất bại! Xe đang có chuyến đi.");
    }
  };

  // --- 5. HÀM MỞ MODAL (QUAN TRỌNG: GỌI API LẤY TÀI XẾ Ở ĐÂY) ---
  const openCreateModal = () => {
    setModalMode("create");
    setFormData({ id: "", licensePlate: "", typeTruck: "", driverId: "", driverName: "", ganMooc: false, status: "AVAILABLE" });
    fetchAvailableDrivers(""); // Truyền rỗng để chỉ lấy người hoàn toàn rảnh
    setIsModalOpen(true);
  };

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
    fetchAvailableDrivers(truck.id); // Lấy người rảnh + tài xế đang lái xe này
    setIsModalOpen(true);
  };

  // --- CÁC HÀM UI KHÁC ---
  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < pagination.totalPages) fetchTrucks(newPage);
  };

  const renderPageNumbers = () => {
    const pages = [];
    let startPage = Math.max(0, pagination.page - 2);
    let endPage = Math.min(pagination.totalPages - 1, pagination.page + 2);
    for (let i = startPage; i <= endPage; i++) {
      pages.push(
        <button key={i} onClick={() => handlePageChange(i)}
          className={`w-8 h-8 rounded-md text-sm font-medium transition ${pagination.page === i ? "bg-blue-600 text-white shadow-sm" : "bg-white border hover:bg-slate-50 text-slate-600"
            }`}
        >
          {i + 1}
        </button>
      );
    }
    return pages;
  };

  const getStatusColor = (status) => {
    switch (status) {
      case "AVAILABLE": return "bg-green-100 text-green-700 border-green-200";
      case "BUSY": return "bg-red-100 text-red-700 border-red-200";
      case "MAINTENANCE": return "bg-yellow-100 text-yellow-700 border-yellow-200";
      default: return "bg-slate-100 text-slate-600 border-slate-200";
    }
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
                <Truck size={24} className="text-white" />
              </div>
              Danh sách Xe tải
            </h1>
            <p className="text-sm text-slate-500 mt-2 ml-14 font-medium">
              Hệ thống hiện có <span className="font-bold text-blue-600">{pagination.total}</span> phương tiện
            </p>
          </div>
          <div className="flex gap-2">
            {canManage && (
              <>
                <button className="flex items-center px-4 py-2.5 bg-emerald-50 text-emerald-700 border border-emerald-200 rounded-xl hover:bg-emerald-100 text-sm font-bold transition shadow-sm">
                  <Upload size={18} className="mr-2" /> Import Excel
                </button>
                <button onClick={openCreateModal} className="flex items-center px-4 py-2.5 bg-blue-600 text-white rounded-xl hover:bg-blue-700 text-sm font-bold transition shadow-lg shadow-blue-200 active:scale-95">
                  <Plus size={18} className="mr-2" /> Thêm xe mới
                </button>
              </>
            )}
          </div>
        </div>

        {/* TABLE */}
        <div className="overflow-x-auto border border-slate-200 rounded-xl shadow-sm">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-slate-50/80 text-slate-500 text-[11px] uppercase tracking-wider border-b border-slate-200">
                <th className="px-6 py-4 font-bold">STT</th>
                <th className="px-6 py-4 font-bold">Biển số</th>
                <th className="px-6 py-4 font-bold">Loại xe</th>
                <th className="px-6 py-4 font-bold">Tài xế phụ trách</th>
                <th className="px-6 py-4 font-bold text-center">Moóc</th>
                <th className="px-6 py-4 font-bold">Trạng thái</th>
                {canManage && <th className="px-6 py-4 font-bold text-right">Thao tác</th>}
              </tr>
            </thead>
            <tbody className="text-sm text-slate-700 divide-y divide-slate-100">
              {loading ? (
                <tr><td colSpan="7" className="text-center py-16 text-slate-400 font-medium animate-pulse">Đang tải dữ liệu...</td></tr>
              ) : trucks.length === 0 ? (
                <tr><td colSpan="7" className="text-center py-16 text-slate-400 font-medium">Chưa có dữ liệu xe tải</td></tr>
              ) : (
                trucks.map((item, index) => (
                  <tr key={item.id || index} className="hover:bg-slate-50/40 transition-colors group">
                    <td className="px-6 py-4 text-slate-400 font-medium">
                      {((pagination.page || 0) * (pagination.size || 10)) + index + 1}
                    </td>
                    <td className="px-6 py-4 font-bold text-slate-900 uppercase text-base">{item.licensePlate}</td>
                    <td className="px-6 py-4">{item.typeTruck || "-"}</td>
                    <td className="px-6 py-4 font-medium text-blue-600">
                      {item.driverName || <span className="text-slate-400 italic">Chưa gán</span>}
                    </td>
                    <td className="px-6 py-4 text-center">
                      {item.ganMooc ? <CheckCircle size={18} className="text-emerald-500 mx-auto" /> : <XCircle size={18} className="text-slate-300 mx-auto" />}
                    </td>
                    <td className="px-6 py-4">
                      <span className={`px-2.5 py-1 rounded-full text-[10px] uppercase font-black border ${getStatusColor(item.status)}`}>
                        {item.status}
                      </span>
                    </td>
                    {canManage && (
                      <td className="px-6 py-4 text-right">
                        <div className="flex items-center justify-end gap-1 transition-opacity">
                          <button onClick={() => openEditModal(item)} className="p-2 text-slate-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition" title="Sửa">
                            <Edit size={16} />
                          </button>
                          <button onClick={() => handleDeleteClick(item.id)} className="p-2 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition" title="Xóa">
                            <Trash2 size={16} />
                          </button>
                        </div>
                      </td>
                    )}
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* FOOTER PAGINATION */}
      {pagination.totalPages > 1 && (
        <div className="flex flex-col sm:flex-row justify-between items-center mt-6 pt-4 border-t border-slate-100 gap-4">
          <div className="text-sm text-slate-500 font-medium">
            Trang <span className="font-semibold text-slate-700">{(pagination.page || 0) + 1}</span> / <span className="font-semibold text-slate-700">{pagination.totalPages || 1}</span>
          </div>
          <div className="flex items-center gap-2">
            <button onClick={() => handlePageChange(pagination.page - 1)} disabled={pagination.page === 0} className="flex items-center p-1.5 border border-slate-200 rounded-lg text-slate-600 hover:bg-slate-50 disabled:opacity-30 transition">
              <ChevronLeft size={18} />
            </button>
            <div className="hidden sm:flex gap-1">{renderPageNumbers()}</div>
            <button onClick={() => handlePageChange(pagination.page + 1)} disabled={pagination.page >= pagination.totalPages - 1} className="flex items-center p-1.5 border border-slate-200 rounded-lg text-slate-600 hover:bg-slate-50 disabled:opacity-30 transition">
              <ChevronRight size={18} />
            </button>
          </div>
        </div>
      )}

      {/* --- MODAL FORM --- */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 backdrop-blur-sm p-4">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md overflow-hidden zoom-in duration-200">
            <div className="bg-slate-800 px-6 py-4 flex justify-between items-center">
              <h3 className="text-white font-bold tracking-wide">
                {modalMode === 'create' ? 'THÊM XE TẢI MỚI' : 'CẬP NHẬT XE TẢI'}
              </h3>
              <button onClick={() => setIsModalOpen(false)} className="text-slate-400 hover:text-white transition"><XCircle size={22} /></button>
            </div>

            <form onSubmit={handleSubmit} className="p-6 space-y-4">
              <div>
                <label className="block text-[11px] font-bold text-slate-500 mb-1 uppercase">Biển số xe <span className="text-red-500">*</span></label>
                <input type="text" required
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none uppercase transition"
                  placeholder="VD: 29C-123.45"
                  value={formData.licensePlate}
                  onChange={(e) => setFormData({ ...formData, licensePlate: e.target.value })}
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-[11px] font-bold text-slate-500 mb-1 uppercase">Loại xe</label>
                  <select className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition text-sm"
                    value={formData.typeTruck}
                    onChange={(e) => setFormData({ ...formData, typeTruck: e.target.value })}
                  >
                    <option value="">-- Chọn loại xe --</option>
                    <option value="Container">Xe Container</option>
                    <option value="Tải nhẹ">Xe Tải nhẹ</option>
                    <option value="Tải nặng">Xe Tải nặng</option>
                    <option value="Đầu kéo">Xe Đầu kéo</option>
                  </select>
                </div>
                
                {/* TRẠNG THÁI */}
                <div>
                  <label className="block text-[11px] font-bold text-slate-500 mb-1 uppercase">Trạng thái</label>
                  <select className="w-full px-3 py-2 border border-slate-300 rounded-lg outline-none text-sm"
                    value={formData.status}
                    onChange={(e) => setFormData({ ...formData, status: e.target.value })}
                  >
                    <option value="AVAILABLE">Sẵn sàng</option>
                    <option value="BUSY">Đang chạy</option>
                    <option value="MAINTENANCE">Bảo trì</option>
                  </select>
                </div>
              </div>

              {/* TÀI XẾ PHỤ TRÁCH DROPDOWN */}
              <div>
                <label className="block text-[11px] font-bold text-slate-500 mb-1 uppercase">Tài xế phụ trách</label>
                <select
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition text-sm"
                  value={formData.driverId || ""}
                  onChange={(e) => {
                    const selectedId = e.target.value;
                    const selectedDriver = drivers.find(d => d.id === selectedId);
                    setFormData({
                      ...formData,
                      driverId: selectedId,
                      driverName: selectedDriver ? selectedDriver.username : ""
                    });
                  }}
                >
                  <option value="">-- Chọn tài xế (Để trống nếu chưa gán) --</option>
                  {drivers && drivers.map((driver) => (
                    <option key={driver.id} value={driver.id}>
                      {driver.username} {driver.phone ? `- ${driver.phone}` : ''}
                    </option>
                  ))}
                </select>
              </div>

              <div className="flex items-center pt-2">
                <input type="checkbox" id="ganMooc" className="w-5 h-5 text-blue-600 rounded mr-2 cursor-pointer"
                  checked={formData.ganMooc}
                  onChange={(e) => setFormData({ ...formData, ganMooc: e.target.checked })}
                />
                <label htmlFor="ganMooc" className="text-sm font-bold text-slate-700 select-none cursor-pointer">Đã gắn Moóc?</label>
              </div>

              <div className="pt-4 flex justify-end gap-3">
                <button type="button" onClick={() => setIsModalOpen(false)} className="px-5 py-2 border border-slate-200 rounded-lg text-slate-600 font-bold text-sm hover:bg-slate-50 transition">HỦY</button>
                <button type="submit" className="px-5 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-bold text-sm flex items-center shadow-lg shadow-blue-200 transition">
                  <Save size={18} className="mr-2" /> LƯU LẠI
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* --- MODAL XÁC NHẬN XÓA --- */}
      {isDeleteModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-sm p-4">
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-sm overflow-hidden p-6 text-center">
            <div className="w-16 h-16 bg-rose-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <AlertTriangle size={32} className="text-rose-600" />
            </div>
            <h3 className="text-xl font-bold text-slate-800 mb-2">Xác nhận xóa xe tải</h3>
            <p className="text-slate-500 text-sm mb-6">
              Bạn có chắc chắn muốn xóa phương tiện này không?<br />Dữ liệu sẽ bị mất vĩnh viễn.
            </p>
            <div className="flex justify-center gap-3">
              <button onClick={() => { setIsDeleteModalOpen(false); setItemToDelete(null); }} className="px-5 py-2 border border-slate-200 rounded-lg hover:bg-slate-50 font-bold text-slate-600 text-sm transition">
                HỦY
              </button>
              <button onClick={confirmDelete} className="px-5 py-2 bg-rose-600 text-white rounded-lg hover:bg-rose-700 font-bold text-sm transition shadow-lg shadow-rose-200">
                XÓA NGAY
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default TrucksPage;