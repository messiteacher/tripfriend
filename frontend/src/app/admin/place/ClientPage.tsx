"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";

interface Place {
  id: number;
  cityName: string;
  placeName: string;
  description: string;
  category: string;
  imageUrl: string;
}

// 영어 카테고리 -> 한글 매핑 객체
const categoryMapping: Record<string, string> = {
  PLACE: "장소",
  STAY: "숙박",
  RESTAURANT: "식당",
  CAFE: "카페",
  ETC: "기타",
};

export default function ClientPage() {
  const [places, setPlaces] = useState<Place[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedCity, setSelectedCity] = useState<string>("");
  const [selectedCategory, setSelectedCategory] = useState<string>("");
  const router = useRouter();

  useEffect(() => {
    fetch(`${process.env.NEXT_PUBLIC_API_URL}/place`)
      .then((res) => res.json())
      .then((data) => {
        console.log("API Response:", data);
        if (data && data.data) {
          const mappedPlaces: Place[] = data.data.map((place: any) => ({
            id: place.id,
            cityName: place.cityName,
            placeName: place.placeName,
            description: place.description,
            category: place.category,
            imageUrl: place.imageUrl,
          }));
          setPlaces(mappedPlaces);
        }
      })
      .catch((error) => console.error("Error fetching places:", error))
      .finally(() => setLoading(false));
  }, []);

  // 선택된 도시와 카테고리에 따라 필터링
  const filteredPlaces = places.filter(
    (place) =>
      (!selectedCity || place.cityName === selectedCity) &&
      (!selectedCategory || place.category === selectedCategory)
  );

  const handleDelete = (id: number) => {
    if (!confirm("정말 삭제하시겠습니까?")) return;
    // 관리자 토큰을 헤더에 포함하여 요청
    const token = localStorage.getItem("accessToken");
    fetch(`${process.env.NEXT_PUBLIC_API_URL}/place/${id}`, {
      method: "DELETE",
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })
      .then((res) => {
        if (!res.ok) {
          throw new Error("삭제에 실패했습니다.");
        }
        // 삭제가 성공하면 state에서 해당 여행지를 제거합니다.
        setPlaces((prev) => prev.filter((place) => place.id !== id));
      })
      .catch((error) => console.error("Error deleting place:", error));
  };

  if (loading) {
    return <p>여행지 데이터를 불러오는 중...</p>;
  }

  return (
    <div>
      <button className="mb-4 text-blue-500" onClick={() => router.back()}>
        뒤로가기
      </button>
      <h2 className="text-2xl font-bold mb-4">전체 여행지</h2>
      <div className="flex items-center gap-4 mb-4">
        <div className="flex gap-4 flex-grow">
          <select
            value={selectedCity}
            onChange={(e) => setSelectedCity(e.target.value)}
            className="p-2 border rounded-md"
          >
            <option value="">모든 도시</option>
            {Array.from(new Set(places.map((place) => place.cityName))).map(
              (city) => (
                <option key={city} value={city}>
                  {city}
                </option>
              )
            )}
          </select>

          <select
            value={selectedCategory}
            onChange={(e) => setSelectedCategory(e.target.value)}
            className="p-2 border rounded-md"
          >
            <option value="">모든 카테고리</option>
            {Array.from(new Set(places.map((place) => place.category))).map(
              (category) => (
                <option key={category} value={category}>
                  {categoryMapping[category] || category}
                </option>
              )
            )}
          </select>
        </div>

        <button
          className="bg-green-500 text-white px-4 py-2 rounded-md"
          onClick={() => router.push("/admin/place/create")}
        >
          여행지 등록
        </button>
      </div>

      {/* 여행지 리스트 */}
      {filteredPlaces.length === 0 ? (
        <p className="text-gray-500">해당 조건에 맞는 여행지가 없습니다.</p>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredPlaces.map((place) => (
            <div
              key={place.id}
              className="bg-white rounded-lg shadow-md p-4 relative"
            >
              <img
                src={
                  place.imageUrl
                    ? `${process.env.NEXT_PUBLIC_API_URL}${place.imageUrl}`
                    : "/default-placeholder.svg"
                }
                alt={place.cityName}
                className="w-full aspect-[4/3] object-cover rounded-md cursor-pointer"
                onClick={() => router.push(`/place/${place.id}`)}
              />
              <h3
                className="text-xl font-semibold mt-2 text-blue-500 cursor-pointer"
                onClick={() => router.push(`/place/${place.id}`)}
              >
                {place.placeName}
              </h3>
              <p className="text-gray-600">{place.description}</p>
              {/* 카테고리 한글값 표시 */}
              <p className="mt-2 text-sm text-gray-700">
                카테고리: {categoryMapping[place.category] || place.category}
              </p>
              <button
                className="absolute top-2 right-2 bg-red-500 text-white px-2 py-1 rounded-md"
                onClick={() => handleDelete(place.id)}
              >
                삭제
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
