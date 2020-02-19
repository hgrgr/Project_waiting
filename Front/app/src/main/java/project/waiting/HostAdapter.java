package project.waiting;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class HostAdapter extends RecyclerView.Adapter<HostAdapter.ViewHolder>{
    public class Datas{//클릭 할때 전해줄 데이터들
        public Datas(String name,String la, String lo){
            hostname =name;
            latitude=la;
            lotitude=lo;
        }
        public String hostname;
        public String latitude;
        public String lotitude;
    };
    ArrayList<Host> items = new ArrayList<Host>();

    public interface OnitemClickListener{
        void onItemClick(View v, int pos,int bit,Datas data);
    }
    private OnitemClickListener myListener = null;

    public void setOnItemClickListener(OnitemClickListener listener){
        this.myListener=listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {//hostview에 담아 인플레이터 시킴
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.hostview,parent,false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Host item = items.get(position);//빈홀더 보여줄 host 정보 넣음
        holder.setItem(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(Host item){
        items.add(item);
    }

    public void setItems(ArrayList<Host> items){
        this.items =items;
    }
    public Host getItem(int position){
        return items.get(position);
    }
    public void clear(){
        items.clear();
    }
    public  class ViewHolder extends RecyclerView.ViewHolder{
        TextView hostname;
        TextView address;
        TextView address2;
        Button map;
        Button waiting;
        Button delete;
        Datas data;
        public ViewHolder(View itemView){
            super(itemView);
            hostname = itemView.findViewById(R.id.hostname);
            address = itemView.findViewById(R.id.address);
            address2 = itemView.findViewById(R.id.address2);
            map = itemView.findViewById(R.id.map);
            waiting= itemView.findViewById(R.id.waiting);
            delete =itemView.findViewById(R.id.delete);

            itemView.setOnClickListener(new View.OnClickListener() {//객체별로 비트 부여해서 다르게 처리 해준다 -> 이렇게 안하면 두번 클릭해야함

                @Override
                public void onClick(View v) {//카드뷰 클릭시
                    int pos = getAdapterPosition();
                    if(pos!=RecyclerView.NO_POSITION){//만든 리스너에 알려준다
                        if(myListener !=null){
                            myListener.onItemClick(v, pos,0,data);
                        }
                    }
                }
            });
            map.setOnClickListener(new View.OnClickListener() {//map 버튼 클릭시
                @Override
                public void onClick(View v) {//지도 클릭시
                    int pos = getAdapterPosition();
                    if(pos!=RecyclerView.NO_POSITION){//만든 리스너에 알려준다
                        if(myListener !=null){
                            myListener.onItemClick(v, pos,1,data);//여기서 v는 host 객체가 아니고 host안에 button map 의 객체임
                        }
                    }
                }
            });
            waiting.setOnClickListener(new View.OnClickListener() { //예약현황 클릭시
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if(pos!=RecyclerView.NO_POSITION){//만든 리스너에 알려준다
                        if(myListener !=null){
                            myListener.onItemClick(v, pos,2,data);
                        }
                    }
                }
            });
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if(pos!=RecyclerView.NO_POSITION){//만든 리스너에 알려준다
                        if(myListener !=null){
                            myListener.onItemClick(v, pos,3,data);
                        }
                    }
                }
            });

        }

        public void setItem(Host item){
            hostname.setText(item.hostname);
            address.setText(item.latitude);
            address2.setText(item.lotitude);
            data = new Datas(hostname.getText().toString(), address.getText().toString(),address2.getText().toString());
        }
    }
}
