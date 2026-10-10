const BASE_URL = 'http://localhost:8080/api/v1';
// Giả lập USER_ID của quản lý
const MOCK_USER_ID = '123e4567-e89b-12d3-a456-426614174000'; 
const MOCK_BRANCH_ID = '123e4567-e89b-12d3-a456-426614174001';

export const fetchServices = async () => {
  try {
    const res = await fetch(`${BASE_URL}/branches/${MOCK_BRANCH_ID}/services`, {
      headers: {
        'X-User-Id': MOCK_USER_ID,
      },
    });
    if (!res.ok) throw new Error('Network response was not ok');
    const data = await res.json();
    return data.content || data; // Handle page response vs list
  } catch (error) {
    console.error("Failed to fetch services:", error);
    return [];
  }
};

export const createMeterReading = async (roomId: string, currentElectricity: number, currentWater: number, period: string) => {
  try {
    const res = await fetch(`${BASE_URL}/meter-readings`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-User-Id': MOCK_USER_ID,
      },
      body: JSON.stringify({
        roomId,
        currentElectricityIndicator: currentElectricity,
        currentWaterIndicator: currentWater,
        readingDate: new Date().toISOString().split('T')[0],
        billingPeriodStart: `${period}-01`, // e.g. 2024-05-01
        billingPeriodEnd: `${period}-28`, 
      })
    });
    if (!res.ok) throw new Error('Network response was not ok');
    return await res.json();
  } catch (error) {
    console.error("Failed to submit meter reading:", error);
    throw error;
  }
};

export const fetchUnreadNotificationsCount = async () => {
  try {
    const res = await fetch(`${BASE_URL}/notifications/unread-count`, {
      headers: {
        'X-User-Id': MOCK_USER_ID,
      },
    });
    if (!res.ok) throw new Error('Network response was not ok');
    return await res.json();
  } catch (error) {
    console.error("Failed to fetch notification count:", error);
    return { unreadCount: 0 };
  }
};

export const createService = async (serviceData: { name: string, serviceType: string, unit: string, billingMethod: string, defaultUnitPrice: number, description: string }) => {
  try {
    const res = await fetch(`${BASE_URL}/services`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-User-Id': MOCK_USER_ID,
      },
      body: JSON.stringify({
        ...serviceData,
        branchId: MOCK_BRANCH_ID
      })
    });
    if (!res.ok) throw new Error('Network response was not ok');
    return await res.json();
  } catch (error) {
    console.error("Failed to create service:", error);
    throw error;
  }
};
