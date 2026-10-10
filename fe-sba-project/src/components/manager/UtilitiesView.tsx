import React, { useState, useEffect } from 'react';
import { fetchServices, createMeterReading, createService } from '../../api/utilitiesApi';

export const UtilitiesView: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'readings' | 'services' | 'notifications'>('readings');
  const [services, setServices] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [isAddingService, setIsAddingService] = useState(false);
  const [newService, setNewService] = useState({ name: '', serviceType: 'OTHER', unit: 'tháng', billingMethod: 'FIXED', defaultUnitPrice: 0, description: '' });

  useEffect(() => {
    if (activeTab === 'services') {
      loadServices();
    }
  }, [activeTab]);

  const loadServices = async () => {
    setLoading(true);
    try {
      const data = await fetchServices();
      if (data && data.length > 0) {
        setServices(data);
      }
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  const handleSaveService = async () => {
    try {
      await createService(newService);
      alert('Thêm dịch vụ thành công!');
      setIsAddingService(false);
      loadServices();
    } catch (e) {
      alert('Thêm dịch vụ thất bại. Xem console log.');
    }
  };

  const handleSaveReading = async (roomId: string, elec: number, water: number) => {
    try {
      await createMeterReading(roomId, elec, water, '2024-05');
      alert(`Đã lưu chỉ số điện/nước cho phòng thành công qua API!`);
    } catch (e) {
      alert(`Lưu thất bại. Backend có thể chưa chạy hoặc dữ liệu không hợp lệ.`);
    }
  };

  return (
    <div className="p-6 max-w-7xl mx-auto flex flex-col gap-6 animate-in fade-in duration-300">
      <div className="flex flex-col gap-2">
        <h1 className="text-2xl font-bold font-headline text-primary">Điện nước & Dịch vụ</h1>
        <p className="text-sm text-on-surface-variant">Quản lý chỉ số điện nước, phân bổ chi phí dịch vụ và gửi thông báo cho khách thuê (Đã nối API).</p>
      </div>

      {/* Tabs */}
      <div className="flex bg-surface-container-low rounded-xl p-1 gap-1 border border-outline-variant/30 w-fit">
        <button
          onClick={() => setActiveTab('readings')}
          className={`px-4 py-2 rounded-lg text-sm font-semibold transition-all flex items-center gap-2 ${
            activeTab === 'readings'
              ? 'bg-primary text-white shadow-sm'
              : 'text-on-surface-variant hover:bg-surface-container-high'
          }`}
        >
          <span className="material-symbols-outlined text-[20px]">water_drop</span>
          Chốt điện nước
        </button>
        <button
          onClick={() => setActiveTab('services')}
          className={`px-4 py-2 rounded-lg text-sm font-semibold transition-all flex items-center gap-2 ${
            activeTab === 'services'
              ? 'bg-primary text-white shadow-sm'
              : 'text-on-surface-variant hover:bg-surface-container-high'
          }`}
        >
          <span className="material-symbols-outlined text-[20px]">room_service</span>
          Dịch vụ & Chi phí
        </button>
        <button
          onClick={() => setActiveTab('notifications')}
          className={`px-4 py-2 rounded-lg text-sm font-semibold transition-all flex items-center gap-2 ${
            activeTab === 'notifications'
              ? 'bg-primary text-white shadow-sm'
              : 'text-on-surface-variant hover:bg-surface-container-high'
          }`}
        >
          <span className="material-symbols-outlined text-[20px]">notifications_active</span>
          Thông báo (Notification)
        </button>
      </div>

      {/* Content */}
      <div className="bg-surface-container-lowest rounded-2xl border border-outline-variant/30 p-6 shadow-sm min-h-[400px]">
        {activeTab === 'readings' && (
          <div className="flex flex-col gap-4">
            <h2 className="text-lg font-bold text-on-surface mb-2">Ghi nhận chỉ số điện nước tháng này</h2>
            <div className="overflow-x-auto rounded-xl border border-outline-variant/30">
              <table className="w-full text-left text-sm whitespace-nowrap">
                <thead className="bg-surface-container-low text-on-surface-variant">
                  <tr>
                    <th className="px-4 py-3 font-semibold">Phòng (ID)</th>
                    <th className="px-4 py-3 font-semibold">Khách thuê</th>
                    <th className="px-4 py-3 font-semibold">Số điện cũ</th>
                    <th className="px-4 py-3 font-semibold">Số điện mới</th>
                    <th className="px-4 py-3 font-semibold">Số nước cũ</th>
                    <th className="px-4 py-3 font-semibold">Số nước mới</th>
                    <th className="px-4 py-3 font-semibold">Hành động</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-outline-variant/30">
                  <tr className="hover:bg-surface-container/50">
                    <td className="px-4 py-3 font-bold">123e4567-e89b-12d3-a456-426614174011 (P.201)</td>
                    <td className="px-4 py-3">Nguyễn Văn A</td>
                    <td className="px-4 py-3 text-on-surface-variant">1250</td>
                    <td className="px-4 py-3">
                      <input type="number" id="elec-1" className="w-20 px-2 py-1 border border-outline-variant rounded bg-surface-container-lowest text-sm" defaultValue="1300" />
                    </td>
                    <td className="px-4 py-3 text-on-surface-variant">120</td>
                    <td className="px-4 py-3">
                      <input type="number" id="water-1" className="w-20 px-2 py-1 border border-outline-variant rounded bg-surface-container-lowest text-sm" defaultValue="125" />
                    </td>
                    <td className="px-4 py-3">
                      <button 
                        onClick={() => {
                          const elec = parseInt((document.getElementById('elec-1') as HTMLInputElement).value) || 0;
                          const water = parseInt((document.getElementById('water-1') as HTMLInputElement).value) || 0;
                          handleSaveReading('123e4567-e89b-12d3-a456-426614174011', elec, water);
                        }}
                        className="px-4 py-1.5 bg-primary text-white rounded-lg text-xs font-bold hover:bg-primary-container hover:text-on-primary-container transition-colors shadow-sm w-fit"
                      >
                        Lưu
                      </button>
                    </td>
                  </tr>
                  <tr className="hover:bg-surface-container/50">
                    <td className="px-4 py-3 font-bold">123e4567-e89b-12d3-a456-426614174012 (P.202)</td>
                    <td className="px-4 py-3">Trần Thị B</td>
                    <td className="px-4 py-3 text-on-surface-variant">890</td>
                    <td className="px-4 py-3">
                      <input type="number" id="elec-2" className="w-20 px-2 py-1 border border-outline-variant rounded bg-surface-container-lowest text-sm" defaultValue="920" />
                    </td>
                    <td className="px-4 py-3 text-on-surface-variant">85</td>
                    <td className="px-4 py-3">
                      <input type="number" id="water-2" className="w-20 px-2 py-1 border border-outline-variant rounded bg-surface-container-lowest text-sm" defaultValue="88" />
                    </td>
                    <td className="px-4 py-3">
                      <button 
                        onClick={() => {
                          const elec = parseInt((document.getElementById('elec-2') as HTMLInputElement).value) || 0;
                          const water = parseInt((document.getElementById('water-2') as HTMLInputElement).value) || 0;
                          handleSaveReading('123e4567-e89b-12d3-a456-426614174012', elec, water);
                        }}
                        className="px-4 py-1.5 bg-primary text-white rounded-lg text-xs font-bold hover:bg-primary-container hover:text-on-primary-container transition-colors shadow-sm w-fit"
                      >
                        Lưu
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        )}

        {activeTab === 'services' && (
          <div className="flex flex-col gap-4">
            <h2 className="text-lg font-bold text-on-surface mb-2">Cấu hình dịch vụ cơ sở (Tải từ API Backend)</h2>
            {loading ? (
              <p className="text-sm text-on-surface-variant">Đang tải dữ liệu từ backend...</p>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {services.length > 0 ? services.map(srv => (
                  <div key={srv.id} className="p-4 border border-outline-variant/30 rounded-xl bg-surface-container-lowest">
                    <div className="flex justify-between items-center mb-2">
                      <h3 className="font-semibold">{srv.name}</h3>
                      <span className="text-secondary font-bold">{(srv.defaultUnitPrice || 0).toLocaleString('vi-VN')}đ / {srv.unit}</span>
                    </div>
                    <p className="text-xs text-on-surface-variant">{srv.description || 'Dịch vụ phân bổ'}</p>
                  </div>
                )) : (
                  <>
                    <div className="p-4 border border-outline-variant/30 rounded-xl">
                      <div className="flex justify-between items-center mb-2">
                        <h3 className="font-semibold">Rác sinh hoạt (Mock fallback)</h3>
                        <span className="text-secondary font-bold">50.000đ / phòng</span>
                      </div>
                      <p className="text-xs text-on-surface-variant">Chưa thể kết nối tới BE, hiển thị dữ liệu mẫu.</p>
                    </div>
                    <div className="p-4 border border-outline-variant/30 rounded-xl">
                      <div className="flex justify-between items-center mb-2">
                        <h3 className="font-semibold">Wifi / Internet (Mock fallback)</h3>
                        <span className="text-secondary font-bold">100.000đ / phòng</span>
                      </div>
                      <p className="text-xs text-on-surface-variant">Chưa thể kết nối tới BE, hiển thị dữ liệu mẫu.</p>
                    </div>
                  </>
                )}
              </div>
            )}
            {isAddingService ? (
              <div className="p-4 border border-outline-variant/30 rounded-xl bg-surface-container flex flex-col gap-3">
                <h3 className="font-bold">Thêm dịch vụ mới</h3>
                <div className="grid grid-cols-2 gap-3">
                  <input type="text" placeholder="Tên dịch vụ (VD: Phí quản lý)" className="p-2 text-sm rounded border bg-surface-container-lowest" value={newService.name} onChange={e => setNewService({...newService, name: e.target.value})} />
                  <select className="p-2 text-sm rounded border bg-surface-container-lowest" value={newService.serviceType} onChange={e => setNewService({...newService, serviceType: e.target.value})}>
                    <option value="ELECTRICITY">Điện</option>
                    <option value="WATER">Nước</option>
                    <option value="INTERNET">Internet</option>
                    <option value="PARKING">Gửi xe</option>
                    <option value="CLEANING">Vệ sinh</option>
                    <option value="SECURITY">An ninh</option>
                    <option value="OTHER">Khác</option>
                  </select>
                  <input type="number" placeholder="Đơn giá (VD: 50000)" className="p-2 text-sm rounded border bg-surface-container-lowest" value={newService.defaultUnitPrice || ''} onChange={e => setNewService({...newService, defaultUnitPrice: Number(e.target.value)})} />
                  <input type="text" placeholder="Đơn vị (VD: phòng, người)" className="p-2 text-sm rounded border bg-surface-container-lowest" value={newService.unit} onChange={e => setNewService({...newService, unit: e.target.value})} />
                  <select className="p-2 text-sm rounded border bg-surface-container-lowest" value={newService.billingMethod} onChange={e => setNewService({...newService, billingMethod: e.target.value})}>
                    <option value="FIXED">Cố định</option>
                    <option value="BY_METER">Theo đồng hồ</option>
                    <option value="BY_PERSON">Theo người</option>
                    <option value="BY_ROOM">Theo phòng</option>
                    <option value="PER_USE">Theo lần sử dụng</option>
                  </select>
                  <input type="text" placeholder="Mô tả" className="p-2 text-sm rounded border bg-surface-container-lowest" value={newService.description} onChange={e => setNewService({...newService, description: e.target.value})} />
                </div>
                <div className="flex gap-2 mt-2">
                  <button onClick={handleSaveService} className="px-4 py-1.5 bg-primary text-white text-sm font-bold rounded hover:bg-primary-container hover:text-on-primary-container transition-colors">Lưu dịch vụ</button>
                  <button onClick={() => setIsAddingService(false)} className="px-4 py-1.5 bg-surface-container-high text-sm font-bold rounded hover:bg-surface-container-highest transition-colors">Hủy</button>
                </div>
              </div>
            ) : (
              <button onClick={() => setIsAddingService(true)} className="mt-4 px-4 py-2 bg-primary text-white rounded-lg font-semibold w-fit text-sm hover:bg-primary-container hover:text-on-primary-container transition-colors shadow-sm">
                + Thêm dịch vụ mới
              </button>
            )}
          </div>
        )}

        {activeTab === 'notifications' && (
          <div className="flex flex-col gap-4 max-w-2xl">
            <h2 className="text-lg font-bold text-on-surface mb-2">Gửi thông báo (Notification)</h2>
            <div className="flex flex-col gap-3">
              <div className="flex flex-col gap-1">
                <label className="text-xs font-semibold text-on-surface-variant">Loại thông báo</label>
                <select className="p-2.5 rounded-lg border border-outline-variant/30 bg-surface-container-lowest text-sm outline-none focus:border-primary">
                  <option>Thông báo tiền nhà / hóa đơn</option>
                  <option>Nhắc nhở hợp đồng sắp hết hạn</option>
                  <option>Cập nhật sửa chữa / bảo trì</option>
                  <option>Thông báo chung tới toàn bộ cư dân</option>
                </select>
              </div>
              <div className="flex flex-col gap-1">
                <label className="text-xs font-semibold text-on-surface-variant">Nội dung</label>
                <textarea 
                  className="p-3 rounded-lg border border-outline-variant/30 bg-surface-container-lowest text-sm outline-none focus:border-primary min-h-[120px]"
                  placeholder="Nhập nội dung thông báo muốn gửi..."
                ></textarea>
              </div>
              <div className="flex gap-4 items-center">
                <label className="flex items-center gap-2 text-sm cursor-pointer">
                  <input type="checkbox" className="w-4 h-4 rounded text-primary" defaultChecked />
                  Gửi qua Zalo OA
                </label>
                <label className="flex items-center gap-2 text-sm cursor-pointer">
                  <input type="checkbox" className="w-4 h-4 rounded text-primary" defaultChecked />
                  Gửi qua Email
                </label>
              </div>
              <button 
                onClick={() => alert('Đã giả lập gọi API Backend Notification!')}
                className="mt-2 px-4 py-2.5 bg-primary text-white rounded-lg font-bold w-fit text-sm flex items-center gap-2 hover:bg-primary-container hover:text-on-primary-container transition-colors"
              >
                <span className="material-symbols-outlined text-lg">send</span>
                Phát thông báo
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
