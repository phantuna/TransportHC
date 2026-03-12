import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { 
  Edit, Trash2, Plus, Upload, ChevronLeft, ChevronRight, 
  MapPin, Calendar, FileText, CheckCircle, RotateCcw, ArrowRight, Paperclip, XCircle 
} from 'lucide-react';
import { toast } from 'react-toastify';

const API_URL = 'http://localhost:8081';

const SchedulesPage = () => {
  const [schedules, setSchedules] = useState([]);
  const [loading, setLoading] = useState(true);
  
  // State phân trang
  const [pagination, setPagination] = useState({ 
    page: 0, size: 10, total: 0, totalPages: 1 
  });

  // State Modal (Tạo/Sửa)
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalMode, setModalMode] = useState('create');
  
  // State Modal (Upload)
  const [isUploadModalOpen, setIsUploadModalOpen] = useState(false);
  const [uploadFile, setUploadFile] = useState(null);
  const [selectedScheduleId, setSelectedScheduleId] = useState(null);

  // Form Data
  const [formData, setFormData] = useState({
    id: '',
    startPlace: '',
    endPlace: '',
    expense: '',
    description: ''
  });

  // --- 1. API: LẤY DANH SÁCH ---
  const fetchSchedules = async (page = 0) => {
    setLoading(true);
    try {
      const token = localStorage.getItem('token');
      const response = await axios.get(`${API_URL}/schedule/getAll`, {
        headers: { Authorization: `Bearer ${token}` },
        params: { page, size: pagination.size }
      });
      
      const data = response.data;
      const listData = data.content || data.result || [];
      setSchedules(listData);

      // Fix lỗi pagination giống trang Truck
      const currentPage = data.page ?? data.number ?? page;
      
      setPagination({
        page: currentPage,
        size: data.size ?? 10,
        total: data.totalElements ?? 0,
        totalPages: data.totalPages ?? 1
      });
    } catch (error) {
      console.error(error);
      toast.error('Không thể tải danh sách lịch trình');
    } finally {
      setLoading(false);
    }
  };

  // --- 2. API: DUYỆT / HỦY DUYỆT (LOGIC JAVA) ---
  const handleApproval = async (id, currentStatus) => {
    try {
      const token = localStorage.getItem('token');
      
      // Logic Java: Nếu đang PENDING -> Gửi APPROVED. Nếu đang APPROVED -> Gửi PENDING_APPROVAL
      // Lưu ý: Java không hỗ trợ REJECTED trong đoạn code bạn gửi
      const targetStatus = currentStatus === 'APPROVED' ? 'PENDING_APPROVAL' : 'APPROVED';
      
      const url = `${API_URL}/schedule/approval/${id}?status=${targetStatus}`;
      
      await axios.post(url, null, {
        headers: { Authorization: `Bearer ${token}` }
      });
      
      toast.success(targetStatus === 'APPROVED' ? 'Đã duyệt thành công!' : 'Đã hủy duyệt!');
      fetchSchedules(pagination.page); 
    } catch (error) {
      console.error("Approval Error:", error);
      const msg = error.response?.data?.message || "Thao tác thất bại!";
      toast.error(`Lỗi: ${msg}`);
    }
  };

  // --- 3. API: XÓA (Endpoint: /deleted/{id}) ---
  const handleDelete = async (id) => {
    if (!window.confirm("Bạn có chắc chắn muốn xóa lịch trình này?")) return;
    try {
      const token = localStorage.getItem('token');
      await axios.delete(`${API_URL}/schedule/deleted/${id}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      toast.success("Xóa thành công!");
      fetchSchedules(pagination.page);
    } catch (error) {
      toast.error("Xóa thất bại! Có thể dữ liệu đang được sử dụng.");
    }
  };

  // --- 4. API: TẠO MỚI / CẬP NHẬT ---
  const handleSubmit = async (e) => {
    e.preventDefault();
    const token = localStorage.getItem('token');
    
    const payload = {
      startPlace: formData.startPlace,
      endPlace: formData.endPlace,
      expense: Number(formData.expense),
      description: formData.description
    };

    try {
      if (modalMode === 'create') {
        await axios.post(`${API_URL}/schedule/created`, payload, {
          headers: { Authorization: `Bearer ${token}` }
        });
        toast.success("Tạo lịch trình thành công!");
      } else {
        await axios.put(`${API_URL}/schedule/updated/${formData.id}`, payload, {
          headers: { Authorization: `Bearer ${token}` }
        });
        toast.success("Cập nhật thành công!");
      }
      setIsModalOpen(false);
      fetchSchedules(0); 
    } catch (error) {
      const msg = error.response?.data?.message || "Có lỗi xảy ra!";
      toast.error(msg);
    }
  };

  // --- 5. API: UPLOAD TÀI LIỆU ---
  const handleUploadSubmit = async (e) => {
    e.preventDefault();
    if (!uploadFile || !selectedScheduleId) {
        toast.warning("Vui lòng chọn file!");
        return;
    }
    
    // Check size 5MB giống Java
    if (uploadFile.size > 5 * 1024 * 1024) {
        toast.error("File quá lớn! Vui lòng chọn file < 5MB");
        return;
    }

    const token = localStorage.getItem('token');
    const formDataUpload = new FormData();
    formDataUpload.append('file', uploadFile);

    try {
      await axios.post(`${API_URL}/schedule/${selectedScheduleId}/documents`, formDataUpload, {
        headers: { 
          Authorization: `Bearer ${token}`,
          'Content-Type': 'multipart/form-data'
        }
      });
      toast.success("Upload tài liệu thành công!");
      setIsUploadModalOpen(false);
      setUploadFile(null);
    } catch (error) {
      console.error(error);
      toast.error("Upload thất bại!");
    }
  };

  // --- HELPER & UI LOGIC ---
  const openCreateModal = () => {
    setModalMode('create');
    setFormData({ id: '', startPlace: '', endPlace: '', expense: '', description: '' });
    setIsModalOpen(true);
  };

  const openEditModal = (item) => {
    setModalMode('edit');
    setFormData({
      id: item.id,
      startPlace: item.startPlace,
      endPlace: item.endPlace,
      expense: item.expense,
      description: item.description
    });
    setIsModalOpen(true);
  };

  const openUploadModal = (id) => {
    setSelectedScheduleId(id);
    setUploadFile(null);
    setIsUploadModalOpen(true);
  }

  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < pagination.totalPages) fetchSchedules(newPage);
  };

  useEffect(() => { fetchSchedules(0); }, []);

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount || 0);
  };

  // Badge trạng thái
  const getApprovalBadge = (status) => {
    if (status === 'APPROVED') 
        return <span className="px-2 py-1 bg-green-100 text-green-700 rounded-full text-xs font-bold border border-green-200">Đã duyệt</span>;
    return <span className="px-2 py-1 bg-yellow-100 text-yellow-700 rounded-full text-xs font-bold border border-yellow-200">Chờ duyệt</span>;
  };

  const renderPageNumbers = () => {
    const pages = [];
    let startPage = Math.max(0, pagination.page - 2);
    let endPage = Math.min(pagination.totalPages - 1, pagination.page + 2);
    for (let i = startPage; i <= endPage; i++) {
      pages.push(
        <button key={i} onClick={() => handlePageChange(i)}
          className={`w-8 h-8 rounded-md text-sm font-medium transition ${pagination.page === i ? 'bg-blue-600 text-white' : 'bg-white border hover:bg-slate-50'}`}>
          {i + 1}
        </button>
      );
    }
    return pages;
  };

  return (
    <div className="bg-white rounded-lg shadow p-6 min-h-[600px] flex flex-col justify-between relative">
      
      {/* HEADER */}
      <div className="flex flex-col md:flex-row justify-between items-center mb-6 gap-4">
        <div>
          <h1 className="text-xl font-bold text-slate-800 flex items-center">
            <Calendar className="mr-2" /> Quản lý Lịch trình
          </h1>
          <p className="text-sm text-slate-500 mt-1">
            Tổng số: <span className="font-bold text-blue-600">{pagination.total}</span> chuyến đi
          </p>
        </div>
        <div className="flex gap-2">
            <button onClick={openCreateModal} className="flex items-center px-3 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 text-sm font-medium transition">
                <Plus size={16} className="mr-2" /> Tạo lịch trình
            </button>
        </div>
      </div>

      {/* TABLE */}
      <div className="overflow-x-auto">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="text-slate-500 border-b border-slate-200 text-xs uppercase bg-slate-50">
              <th className="px-4 py-3 font-semibold">STT</th>
              <th className="px-4 py-3 font-semibold">Hành trình</th>
              <th className="px-4 py-3 font-semibold">Chi phí dự kiến</th>
              <th className="px-4 py-3 font-semibold">Mô tả</th>
              <th className="px-4 py-3 font-semibold text-center">Trạng thái</th>
              <th className="px-4 py-3 font-semibold text-center">Tài liệu</th>
              <th className="px-4 py-3 font-semibold text-center w-40">Thao tác</th>
            </tr>
          </thead>
          <tbody className="text-sm text-slate-700 divide-y divide-slate-100">
            {loading ? (
              <tr><td colSpan="7" className="text-center py-10 text-slate-400">Đang tải dữ liệu...</td></tr>
            ) : schedules.length === 0 ? (
              <tr><td colSpan="7" className="text-center py-10 text-slate-400">Chưa có dữ liệu</td></tr>
            ) : (
              schedules.map((item, index) => (
                <tr key={item.id || index} className="hover:bg-slate-50 transition-colors">
                  <td className="px-4 py-3 text-slate-500">
                    {((pagination.page || 0) * (pagination.size || 10)) + index + 1}
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-2 font-medium text-slate-800">
                        <MapPin size={16} className="text-blue-500" />
                        {item.startPlace} 
                        <ArrowRight size={14} className="text-slate-400" /> 
                        {item.endPlace}
                    </div>
                  </td>
                  <td className="px-4 py-3 font-mono text-slate-600">
                    {formatCurrency(item.expense)}
                  </td>
                  <td className="px-4 py-3 max-w-xs truncate text-slate-500" title={item.description}>
                    {item.description || "-"}
                  </td>
                  <td className="px-4 py-3 text-center">
                    {getApprovalBadge(item.approval || item.status)}
                  </td>
                  <td className="px-4 py-3 text-center">
                    <button onClick={() => openUploadModal(item.id)} className="text-slate-400 hover:text-blue-600 tooltip" title="Upload tài liệu">
                        <Paperclip size={18} className="mx-auto"/>
                    </button>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex items-center justify-center gap-2">
                        {/* LOGIC NÚT DUYỆT:
                           - Nếu đang APPROVED -> Hiện nút "Hủy duyệt" (Màu cam)
                           - Nếu đang PENDING  -> Hiện nút "Duyệt" (Màu xanh)
                        */}
                        {item.approval === 'APPROVED' ? (
                             <button onClick={() => handleApproval(item.id, item.approval)} className="p-1.5 text-orange-500 hover:bg-orange-50 rounded transition" title="Hủy duyệt (Quay lại Pending)">
                                <RotateCcw size={18} />
                             </button>
                        ) : (
                             <button onClick={() => handleApproval(item.id, item.approval || 'PENDING_APPROVAL')} className="p-1.5 text-green-500 hover:bg-green-50 rounded transition" title="Duyệt">
                                <CheckCircle size={18} />
                             </button>
                        )}
                        
                        <div className="w-px h-4 bg-slate-300 mx-1"></div>

                        <button onClick={() => openEditModal(item)} className="p-1.5 text-slate-400 hover:text-blue-600 hover:bg-blue-50 rounded transition">
                            <Edit size={16} />
                        </button>
                        <button onClick={() => handleDelete(item.id)} className="p-1.5 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded transition">
                            <Trash2 size={16} />
                        </button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* FOOTER PAGINATION */}
      <div className="flex flex-col sm:flex-row justify-between items-center mt-6 pt-4 border-t border-slate-100 gap-4">
        <div className="text-sm text-slate-500">
          Trang <span className="font-semibold text-slate-700">{(pagination.page || 0) + 1}</span> / <span className="font-semibold text-slate-700">{pagination.totalPages || 1}</span>
        </div>
        <div className="flex items-center gap-2">
          <button onClick={() => handlePageChange(pagination.page - 1)} disabled={pagination.page === 0} className="flex items-center px-3 py-1.5 border rounded-md hover:bg-slate-50 disabled:opacity-50">
            <ChevronLeft size={16} className="mr-1" /> Trước
          </button>
          <div className="hidden sm:flex gap-1">{renderPageNumbers()}</div>
          <button onClick={() => handlePageChange(pagination.page + 1)} disabled={pagination.page >= pagination.totalPages - 1} className="flex items-center px-3 py-1.5 border rounded-md hover:bg-slate-50 disabled:opacity-50">
            Sau <ChevronRight size={16} className="ml-1" />
          </button>
        </div>
      </div>

      {/* --- MODAL FORM (CREATE / EDIT) --- */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4 animate-fade-in">
          <div className="bg-white rounded-xl shadow-2xl w-full max-w-lg overflow-hidden">
            <div className="bg-slate-900 px-6 py-4 flex justify-between items-center">
              <h3 className="text-white font-bold text-lg">
                {modalMode === 'create' ? 'Tạo Lịch trình mới' : 'Cập nhật Lịch trình'}
              </h3>
              <button onClick={() => setIsModalOpen(false)} className="text-slate-400 hover:text-white transition"><XCircle size={24} /></button>
            </div>
            
            <form onSubmit={handleSubmit} className="p-6 space-y-4">
              <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-1">Điểm đi <span className="text-red-500">*</span></label>
                    <input type="text" required
                      className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
                      placeholder="VD: Hà Nội"
                      value={formData.startPlace}
                      onChange={(e) => setFormData({...formData, startPlace: e.target.value})}
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-1">Điểm đến <span className="text-red-500">*</span></label>
                    <input type="text" required
                      className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
                      placeholder="VD: Hải Phòng"
                      value={formData.endPlace}
                      onChange={(e) => setFormData({...formData, endPlace: e.target.value})}
                    />
                  </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Chi phí dự kiến (VNĐ)</label>
                <input type="number" min="0"
                  className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
                  value={formData.expense}
                  onChange={(e) => setFormData({...formData, expense: e.target.value})}
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Mô tả chi tiết</label>
                <textarea rows="3"
                  className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 outline-none resize-none"
                  placeholder="Ghi chú về hàng hóa, thời gian..."
                  value={formData.description}
                  onChange={(e) => setFormData({...formData, description: e.target.value})}
                ></textarea>
              </div>

              <div className="pt-4 flex justify-end gap-3">
                <button type="button" onClick={() => setIsModalOpen(false)} className="px-4 py-2 border rounded-lg hover:bg-slate-50">Hủy</button>
                <button type="submit" className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium flex items-center shadow-lg">
                  <FileText size={18} className="mr-2" /> Lưu lại
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* --- MODAL UPLOAD --- */}
      {isUploadModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4 animate-fade-in">
           <div className="bg-white rounded-xl shadow-2xl w-full max-w-sm overflow-hidden">
                <div className="bg-slate-900 px-6 py-4">
                     <h3 className="text-white font-bold text-lg">Upload Tài liệu</h3>
                </div>
                <form onSubmit={handleUploadSubmit} className="p-6 space-y-4">
                    <div className="border-2 border-dashed border-slate-300 rounded-lg p-6 text-center hover:bg-slate-50 transition cursor-pointer relative">
                        <input type="file" required 
                            className="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
                            onChange={(e) => setUploadFile(e.target.files[0])}
                        />
                        <div className="flex flex-col items-center pointer-events-none">
                            <Upload size={32} className="text-blue-500 mb-2" />
                            <span className="text-sm font-medium text-slate-600">
                                {uploadFile ? uploadFile.name : "Kéo thả hoặc chọn file"}
                            </span>
                        </div>
                    </div>
                    <div className="flex justify-end gap-3">
                        <button type="button" onClick={() => setIsUploadModalOpen(false)} className="px-3 py-2 text-sm border rounded hover:bg-slate-50">Đóng</button>
                        <button type="submit" className="px-3 py-2 text-sm bg-blue-600 text-white rounded hover:bg-blue-700">Upload</button>
                    </div>
                </form>
           </div>
        </div>
      )}
    </div>
  );
};

export default SchedulesPage;