import React, { useEffect, useState, useCallback } from "react";
import axiosClient from "../api/axiosClient";
import { 
  Edit, 
  Trash2, 
  Plus, 
  Upload, 
  Download, 
  Tags, 
  XCircle, 
  Save, 
  AlertTriangle 
} from "lucide-react";
import { toast } from "react-toastify";
import { hasPermission } from "../utils/auth"; 

const ExpenseTypePage = () => {
  const [types, setTypes] = useState([]);
  const [loading, setLoading] = useState(true);

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalMode, setModalMode] = useState("create");

  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [itemToDelete, setItemToDelete] = useState(null);

  const [formData, setFormData] = useState({
    id: "",
    key: "",
    label: "",
    description: ""
  });

  // --- CẤU HÌNH QUYỀN (PERMISSIONS) CHẶT CHẼ ---
  // Hỗ trợ cả 2 định dạng tên quyền từ BE (VD: MANAGE_EXPENSE hoặc EXPENSE_MANAGE)
  const canManage = hasPermission("MANAGE_EXPENSE") || hasPermission("EXPENSE_MANAGE") || hasPermission("ADMIN");
  const canCreate = hasPermission("CREATE_EXPENSE") || hasPermission("EXPENSE_CREATE") || canManage;
  const canEdit   = hasPermission("UPDATE_EXPENSE") || hasPermission("EXPENSE_UPDATE") || canManage;
  const canDelete = hasPermission("DELETE_EXPENSE") || hasPermission("EXPENSE_DELETE") || canManage;
  
  // Theo backend Service (getExpenseTypes), yêu cầu quyền MANAGE
  const canView = canManage;

  // --- LẤY DỮ LIỆU ---
  const fetchTypes = useCallback(async () => {
    if (!canView) return; 
    
    setLoading(true);
    try {
      const res = await axiosClient.get("/expense-type/getAll");
      setTypes(res.data || []);
    } catch (e) {
      toast.error(e?.response?.data?.message || "Không tải được danh sách loại chi phí");
    } finally {
      setLoading(false);
    }
  }, [canView]);

  useEffect(() => {
    fetchTypes();
  }, [fetchTypes]);

  // --- HELPER MỞ MODAL ---
  const openCreate = () => {
    if (!canCreate) return toast.error("Bạn không có quyền tạo mới!");
    setModalMode("create");
    setFormData({ id: "", key: "", label: "", description: "" });
    setIsModalOpen(true);
  };

  const openEdit = (item) => {
    if (!canEdit) return toast.error("Bạn không có quyền chỉnh sửa!");
    setModalMode("edit");
    setFormData({
      id: item.id,
      key: item.key,
      label: item.label,
      description: item.description || ""
    });
    setIsModalOpen(true);
  };

  // --- XỬ LÝ FORM THÊM/SỬA ---
  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (modalMode === "create") {
        await axiosClient.post("/expense-type/create", formData);
        toast.success("Tạo loại chi phí thành công!");
      } else {
        await axiosClient.put(`/expense-type/update/${formData.id}`, formData);
        toast.success("Cập nhật thành công!");
      }
      setIsModalOpen(false);
      fetchTypes();
    } catch (err) {
      toast.error(err?.response?.data?.message || "Thao tác thất bại!");
    }
  };

  // --- XỬ LÝ XÓA ---
  const confirmDelete = async () => {
    if (!itemToDelete) return;
    try {
      await axiosClient.delete(`/expense-type/delete/${itemToDelete}`);
      toast.success("Đã xóa danh mục chi phí!");
      setIsDeleteModalOpen(false);
      setItemToDelete(null);
      fetchTypes();
    } catch (err) {
      toast.error(err?.response?.data?.message || "Xóa thất bại!");
    }
  };

  // --- UI: TRUY CẬP BỊ TỪ CHỐI ---
  if (!canView) {
    return (
      <div className="flex flex-col items-center justify-center h-64 bg-white rounded-xl shadow-sm border border-slate-200">
        <AlertTriangle size={48} className="text-amber-500 mb-3" />
        <h2 className="text-xl font-bold text-slate-800">Truy cập bị từ chối</h2>
        <p className="text-slate-500 mt-1 font-medium">Bạn không có quyền quản lý cấu hình chi phí.</p>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-xl shadow-sm p-6 min-h-[600px] flex flex-col border border-slate-200">
      
      {/* HEADER */}
      <div className="flex flex-col md:flex-row justify-between items-center mb-6 gap-4">
        <div>
          <h1 className="text-2xl font-extrabold text-slate-800 flex items-center tracking-tight">
            <div className="p-2 bg-blue-600 rounded-lg mr-3 shadow-md shadow-blue-200">
              <Tags size={24} className="text-white" />
            </div>
            Cấu hình Loại chi phí
          </h1>
          <p className="text-sm text-slate-500 mt-2 ml-14 font-medium">
            Hệ thống đang có <span className="font-bold text-blue-600">{types.length}</span> danh mục
          </p>
        </div>
        
        <div className="flex gap-2">
          {canManage && (
            <>
              <button className="flex items-center px-4 py-2.5 bg-slate-50 text-slate-700 border border-slate-200 rounded-xl hover:bg-slate-100 text-sm font-bold transition shadow-sm">
                <Download size={18} className="mr-2" /> Export
              </button>
              <button className="flex items-center px-4 py-2.5 bg-emerald-50 text-emerald-700 border border-emerald-200 rounded-xl hover:bg-emerald-100 text-sm font-bold transition shadow-sm">
                <Upload size={18} className="mr-2" /> Import
              </button>
            </>
          )}

          {canCreate && (
            <button onClick={openCreate} className="flex items-center px-4 py-2.5 bg-blue-600 text-white rounded-xl hover:bg-blue-700 text-sm font-bold transition shadow-lg shadow-blue-200 active:scale-95">
              <Plus size={18} className="mr-2" /> Tạo mới
            </button>
          )}
        </div>
      </div>

      {/* TABLE */}
      <div className="overflow-x-auto border border-slate-200 rounded-xl shadow-sm">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-slate-50/80 text-slate-500 text-[11px] uppercase tracking-wider border-b border-slate-200">
              <th className="px-6 py-4 font-bold">STT</th>
              <th className="px-6 py-4 font-bold">Mã hệ thống (Key)</th>
              <th className="px-6 py-4 font-bold">Tên hiển thị</th>
              <th className="px-6 py-4 font-bold">Mô tả / Ghi chú</th>
              {(canEdit || canDelete) && <th className="px-6 py-4 font-bold text-right">Thao tác</th>}
            </tr>
          </thead>
          <tbody className="text-sm text-slate-700 divide-y divide-slate-100">
            {loading ? (
              <tr>
                <td colSpan={canEdit || canDelete ? "5" : "4"} className="text-center py-16 text-slate-400 font-medium animate-pulse">
                  Đang tải dữ liệu...
                </td>
              </tr>
            ) : types.length === 0 ? (
              <tr>
                <td colSpan={canEdit || canDelete ? "5" : "4"} className="text-center py-16 text-slate-400 font-medium">
                  Chưa có loại chi phí nào được cấu hình.
                </td>
              </tr>
            ) : (
              types.map((t, index) => (
                <tr key={t.id || t.key} className="hover:bg-slate-50/40 transition-colors group">
                  <td className="px-6 py-4 text-slate-400 font-medium">{index + 1}</td>
                  <td className="px-6 py-4 font-bold text-slate-800 font-mono">
                    <span className="px-2.5 py-1 bg-slate-100 text-slate-600 rounded text-[11px] border border-slate-200 uppercase tracking-wider">{t.key}</span>
                  </td>
                  <td className="px-6 py-4 font-bold text-blue-600 text-base">{t.label}</td>
                  <td className="px-6 py-4 text-slate-500">{t.description || <span className="text-slate-300 italic">Không có mô tả</span>}</td>
                  
                  {(canEdit || canDelete) && (
                    <td className="px-6 py-4 text-right">
                      <div className="flex items-center justify-end gap-1">
                        {canEdit && (
                          <button onClick={() => openEdit(t)} className="p-2 text-slate-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition" title="Sửa">
                            <Edit size={16} />
                          </button>
                        )}
                        {canDelete && (
                          <button onClick={() => { setItemToDelete(t.id); setIsDeleteModalOpen(true); }} className="p-2 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition" title="Xóa">
                            <Trash2 size={16} />
                          </button>
                        )}
                      </div>
                    </td>
                  )}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* MODAL THÊM / SỬA */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 backdrop-blur-sm p-4">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md overflow-hidden zoom-in duration-200">
            <div className="bg-slate-800 px-6 py-4 flex justify-between items-center">
              <h3 className="text-white font-bold tracking-wide uppercase">
                {modalMode === 'create' ? 'Tạo Loại Chi Phí' : 'Cập Nhật Chi Phí'}
              </h3>
              <button onClick={() => setIsModalOpen(false)} className="text-slate-400 hover:text-white transition"><XCircle size={22} /></button>
            </div>

            <form onSubmit={handleSubmit} className="p-6 space-y-4">
              <div>
                <label className="block text-[11px] font-bold text-slate-500 mb-1 uppercase">Mã hệ thống (Key) <span className="text-red-500">*</span></label>
                <input type="text" required
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition text-sm font-mono uppercase text-slate-800 disabled:bg-slate-100 disabled:text-slate-400"
                  placeholder="VD: FUEL, REPAIR, TOLL..."
                  value={formData.key} 
                  onChange={(e) => setFormData({ ...formData, key: e.target.value.toUpperCase() })}
                  disabled={modalMode === 'edit'} 
                />
                {modalMode === 'edit' && <span className="block text-[10px] text-amber-600 mt-1 font-medium">⚠️ Không thể thay đổi Mã hệ thống (Key) sau khi tạo.</span>}
              </div>

              <div>
                <label className="block text-[11px] font-bold text-slate-500 mb-1 uppercase">Tên hiển thị <span className="text-red-500">*</span></label>
                <input type="text" required
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition text-sm text-slate-800"
                  placeholder="VD: Nhiên liệu, Sửa chữa..."
                  value={formData.label} 
                  onChange={(e) => setFormData({ ...formData, label: e.target.value })}
                />
              </div>

              <div>
                <label className="block text-[11px] font-bold text-slate-500 mb-1 uppercase">Mô tả chi tiết</label>
                <textarea rows="3"
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition resize-none text-sm text-slate-800"
                  placeholder="Ghi chú về loại chi phí này..."
                  value={formData.description} 
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                />
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

      {/* MODAL XÁC NHẬN XÓA */}
      {isDeleteModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-sm p-4">
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-sm p-6 text-center">
            <div className="w-16 h-16 bg-rose-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <AlertTriangle size={32} className="text-rose-600" />
            </div>
            <h3 className="text-xl font-bold text-slate-800 mb-2">Xác nhận xóa</h3>
            <p className="text-slate-500 text-sm mb-6">Bạn có chắc chắn muốn xóa loại chi phí này? Các giao dịch liên quan có thể bị ảnh hưởng.</p>
            <div className="flex justify-center gap-3">
              <button onClick={() => setIsDeleteModalOpen(false)} className="px-5 py-2 border border-slate-200 rounded-lg text-slate-600 font-bold text-sm hover:bg-slate-50 transition">HỦY</button>
              <button onClick={confirmDelete} className="px-5 py-2 bg-rose-600 text-white rounded-lg hover:bg-rose-700 font-bold text-sm shadow-lg shadow-rose-200 transition">XÓA NGAY</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ExpenseTypePage;