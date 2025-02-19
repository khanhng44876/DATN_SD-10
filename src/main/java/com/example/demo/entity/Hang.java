package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "hang")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Hang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @Column(name = "ten_hang")
    private String tenhang;
    @Column(name = "trang_thai")
    private String trangthai;

}
