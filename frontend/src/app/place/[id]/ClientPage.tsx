"use client";

import { useEffect, useState } from "react";
import { useRouter, useParams } from "next/navigation";

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

export default function PlaceDetailPage() {
  const { id } = useParams(); // URL에서 id 가져오기
  const [place, setPlace] = useState<Place | null>(null);
  const [loading, setLoading] = useState(true);
  const router = useRouter();

  useEffect(() => {
    if (!id) return;

    fetch(`${process.env.NEXT_PUBLIC_API_URL}/place/${id}`)
      .then((res) => res.json())
      .then((data) => {
        if (data && data.data) {
          setPlace(data.data);
        }
      })
      .catch((error) => console.error("Error fetching place details:", error))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) {
    return <p>여행지 정보를 불러오는 중...</p>;
  }

  if (!place) {
    return <p>해당 여행지를 찾을 수 없습니다.</p>;
  }

  return (
    <div className="container mx-auto p-6">
      <button className="mb-4 text-blue-500" onClick={() => router.back()}>
        뒤로가기
      </button>
      <h1 className="text-3xl font-bold mb-4">{place.placeName}</h1>
      <img
        src={
          place.imageUrl
            ? `${process.env.NEXT_PUBLIC_API_URL}${place.imageUrl}`
            : "/default-placeholder.svg"
        }
        alt={place.placeName}
        className="w-full aspect-[4/3] object-cover rounded-md cursor-pointer"
      />
      <p className="mt-4 text-gray-700">{place.description}</p>
      <p className="mt-2 text-sm text-gray-500">도시: {place.cityName}</p>
      <p className="mt-2 text-sm text-gray-500">
        카테고리: {categoryMapping[place.category] || place.category}
      </p>
    </div>
  );
}
