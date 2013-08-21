ahni(아니?)
====

<h1>인하대학교 강의후기 사이트(기획 단계)</h1>

<h2>기본환경</h2>
  <ul>
    <li>메인 프레임워크: Play framework 2</li>
    <li>데이터베이스: MongoDB</li>
    <li>UI/UX: 트위터 부트스트랩 or Flat-UI</li>
    <li>기타요구사항: 소셜 계정 로그인, 반응형 디자인, Mahout recommender(협력적 필터링 기법을 이용한 강의추천 기능)</li>
    <li>재미있는요소: 이달의 후기왕, Badge Collection 시스템(첫번째 후기를 남기면 배지를 줌, 다른 사람의 후기를 추천하면 배지를 줌)</li>
  </ul>

<h2>MongoDB Design</h2>
  <ul>
    <li>users</li>
    <li>reviews</li>
    <li>lectures</li>
    <li>badges</li>
  </ul>